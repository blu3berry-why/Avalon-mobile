package hu.blu3berry.avalon.game

import hu.blu3berry.avalon.core.domain.model.Character
import hu.blu3berry.avalon.core.domain.model.Role
import hu.blu3berry.avalon.core.domain.model.Score
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.lobby.FakeGameRepository
import hu.blu3berry.avalon.lobby.gameInfo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private class FakeAuthRepository(private val username: String?) : AuthRepository {
    override suspend fun login(username: String, password: String): EmptyResult<DataError.Network> =
        Result.Success(Unit)
    override suspend fun register(username: String, password: String, email: String?): EmptyResult<DataError.Network> =
        Result.Success(Unit)
    override suspend fun logout() = Unit
    override suspend fun isLoggedIn(): Boolean = username != null
    override suspend fun currentUsername(): String? = username
}

class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val gameRepository = FakeGameRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm(username: String? = "arthur") =
        GameViewModel(gameRepository, FakeAuthRepository(username), "ABCD")

    /** Emits and drains the scheduler so the collector has fully processed the value. */
    private suspend fun emit(info: hu.blu3berry.avalon.core.domain.model.GameInfo) {
        gameRepository.infoFlow.emit(Result.Success(info))
        dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `derives phases from game info`() = runTest(dispatcher) {
        val vm = vm()
        assertEquals(GamePhase.LOADING, vm.state.value.phase)
        dispatcher.scheduler.advanceUntilIdle()

        emit(gameInfo(king = "arthur", players = listOf("arthur", "morgana"), playerSelectNum = 2))
        assertEquals(GamePhase.TEAM_SELECTION, vm.state.value.phase)
        assertTrue(vm.state.value.isKing)

        emit(gameInfo(selectedForAdventure = listOf("arthur", "morgana")))
        assertEquals(GamePhase.TEAM_VOTE, vm.state.value.phase)

        emit(gameInfo(isAdventure = true, selectedForAdventure = listOf("arthur")))
        assertEquals(GamePhase.ADVENTURE_VOTE, vm.state.value.phase)
        assertTrue(vm.state.value.isOnAdventure)

        emit(gameInfo(scores = listOf(Score.GOOD, Score.GOOD, Score.GOOD)))
        assertEquals(GamePhase.ASSASSIN_GUESS, vm.state.value.phase)

        emit(gameInfo(winner = Winner.GOOD))
        assertEquals(GamePhase.OUTCOME, vm.state.value.phase)
    }

    @Test
    fun `fetches character until it lands, then stops`() = runTest(dispatcher) {
        val vm = vm()
        dispatcher.scheduler.advanceUntilIdle()
        emit(gameInfo())
        assertEquals(null, vm.state.value.character)

        val merlin = Character(role = Role.MERLIN, sees = listOf("morgana"))
        gameRepository.characterResult = Result.Success(merlin)
        emit(gameInfo(currentRound = 1))
        assertEquals(merlin, vm.state.value.character)

        gameRepository.characterResult = Result.Failure(DataError.Network.SERVER_ERROR)
        emit(gameInfo(currentRound = 2))
        assertEquals(merlin, vm.state.value.character)
    }

    @Test
    fun `submit team requires the exact count and sends the selection`() = runTest(dispatcher) {
        val vm = vm()
        dispatcher.scheduler.advanceUntilIdle()
        emit(gameInfo(king = "arthur", players = listOf("arthur", "morgana", "percival"), playerSelectNum = 2))

        vm.onAction(GameAction.ToggleTeamMember("arthur"))
        vm.onAction(GameAction.SubmitTeam)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(null, gameRepository.selectForAdventureArgs)

        vm.onAction(GameAction.ToggleTeamMember("morgana"))
        vm.onAction(GameAction.SubmitTeam)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(setOf("arthur", "morgana"), gameRepository.selectForAdventureArgs?.toSet())
    }

    @Test
    fun `team vote is cast once and resets on a new proposal`() = runTest(dispatcher) {
        val vm = vm()
        dispatcher.scheduler.advanceUntilIdle()
        emit(gameInfo(currentRound = 1, selectedForAdventure = listOf("arthur", "morgana")))

        vm.onAction(GameAction.VoteOnTeam(approve = true))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("arthur" to true, gameRepository.voteOnTeamArgs)
        assertTrue(vm.state.value.teamVoteCast)

        // Same proposal re-polled: the vote stays cast.
        emit(gameInfo(currentRound = 1, selectedForAdventure = listOf("arthur", "morgana")))
        assertTrue(vm.state.value.teamVoteCast)

        // Rejected team, new proposal: fresh vote.
        emit(gameInfo(currentRound = 1, selectedForAdventure = listOf("arthur", "percival")))
        assertFalse(vm.state.value.teamVoteCast)
    }

    @Test
    fun `adventure vote resets on the next adventure`() = runTest(dispatcher) {
        val vm = vm()
        dispatcher.scheduler.advanceUntilIdle()
        emit(gameInfo(isAdventure = true, currentAdventure = 1, selectedForAdventure = listOf("arthur")))

        vm.onAction(GameAction.VoteOnAdventure(succeed = true))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("arthur" to true, gameRepository.voteOnAdventureArgs)
        assertTrue(vm.state.value.adventureVoteCast)

        emit(gameInfo(isAdventure = true, currentAdventure = 2, selectedForAdventure = listOf("arthur")))
        assertFalse(vm.state.value.adventureVoteCast)
    }

    @Test
    fun `merlin guess is sent once`() = runTest(dispatcher) {
        val vm = vm()
        dispatcher.scheduler.advanceUntilIdle()
        emit(gameInfo(scores = listOf(Score.GOOD, Score.GOOD, Score.GOOD)))

        vm.onAction(GameAction.GuessMerlin("percival"))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("percival", gameRepository.merlinGuess)

        gameRepository.merlinGuess = null
        vm.onAction(GameAction.GuessMerlin("arthur"))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(null, gameRepository.merlinGuess)
    }

    @Test
    fun `poll failure surfaces, next success clears it`() = runTest(dispatcher) {
        val vm = vm()
        dispatcher.scheduler.advanceUntilIdle()
        gameRepository.infoFlow.emit(Result.Failure(DataError.Network.SERVICE_UNAVAILABLE))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(DataError.Network.SERVICE_UNAVAILABLE, vm.state.value.error)

        emit(gameInfo())
        assertEquals(null, vm.state.value.error)
    }
}
