package hu.blu3berry.avalon.lobby

import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class LobbyViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val lobbyRepository = FakeLobbyRepository()
    private val gameRepository = FakeGameRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    /**
     * Cancels the ViewModel before runTest's cleanup drains the scheduler — an alive poll
     * loop re-schedules delays forever and the drain would never finish.
     */
    private fun lobbyTest(block: suspend TestScope.(LobbyViewModel) -> Unit) = runTest(dispatcher) {
        val vm = LobbyViewModel(lobbyRepository, gameRepository, "ABCD", pollInterval = 2.seconds)
        try {
            block(vm)
        } finally {
            vm.viewModelScope.cancel()
        }
    }

    @Test
    fun `poll refreshes players and settings load once`() = lobbyTest { vm ->
        lobbyRepository.playerNamesResults.addAll(
            listOf(Result.Success(listOf("arthur")), Result.Success(listOf("arthur", "morgana"))),
        )
        dispatcher.scheduler.advanceTimeBy(1)
        assertEquals(listOf("arthur"), vm.state.value.players)
        assertEquals(defaultSettings, vm.state.value.settings)

        dispatcher.scheduler.advanceTimeBy(2100)
        assertEquals(listOf("arthur", "morgana"), vm.state.value.players)
    }

    @Test
    fun `poll stops and emits GameStarted when the server reports a started game`() = lobbyTest { vm ->
        vm.events.test {
            gameRepository.gameInfoResult = Result.Success(gameInfo(started = true))
            assertEquals(LobbyEvent.GameStarted, awaitItem())
        }
        val callsAtStop = lobbyRepository.playerNamesCalls
        dispatcher.scheduler.advanceTimeBy(10_000)
        assertEquals(callsAtStop, lobbyRepository.playerNamesCalls)
    }

    @Test
    fun `poll stops on 401 without surfacing an error`() = lobbyTest { vm ->
        lobbyRepository.playerNamesResults.add(Result.Failure(DataError.Network.UNAUTHORIZED))
        dispatcher.scheduler.advanceTimeBy(10_000)
        assertEquals(1, lobbyRepository.playerNamesCalls)
        assertEquals(null, vm.state.value.error)
    }

    @Test
    fun `start success emits GameStarted`() = lobbyTest { vm ->
        vm.events.test {
            vm.onAction(LobbyAction.Start)
            assertEquals(LobbyEvent.GameStarted, awaitItem())
        }
    }

    @Test
    fun `start failure surfaces error`() = lobbyTest { vm ->
        lobbyRepository.startResult = Result.Failure(DataError.Network.BAD_REQUEST)
        vm.onAction(LobbyAction.Start)
        dispatcher.scheduler.advanceTimeBy(1)
        assertEquals(DataError.Network.BAD_REQUEST, vm.state.value.error)
    }

    @Test
    fun `leave navigates away even when the request fails`() = lobbyTest { vm ->
        lobbyRepository.leaveResult = Result.Failure(DataError.Network.SERVER_ERROR)
        vm.events.test {
            vm.onAction(LobbyAction.Leave)
            assertEquals(LobbyEvent.Left, awaitItem())
        }
    }

    @Test
    fun `save settings sends the edited copy`() = lobbyTest { vm ->
        dispatcher.scheduler.advanceTimeBy(1)
        val edited = defaultSettings.copy(percival = true)
        vm.onAction(LobbyAction.SettingsChanged(edited))
        vm.onAction(LobbyAction.SaveSettings)
        dispatcher.scheduler.advanceTimeBy(1)
        assertEquals(edited, lobbyRepository.updatedSettings)
    }
}
