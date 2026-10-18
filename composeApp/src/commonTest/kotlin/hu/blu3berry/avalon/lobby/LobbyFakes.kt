package hu.blu3berry.avalon.lobby

import hu.blu3berry.avalon.core.domain.model.Character
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.core.domain.repository.GameRepository
import hu.blu3berry.avalon.core.domain.repository.LobbyRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

val defaultSettings = LobbySettings(
    assassin = true,
    mordred = false,
    morgana = false,
    oberon = false,
    percival = false,
    arnold = false,
)

fun gameInfo(
    started: Boolean = true,
    winner: Winner = Winner.NOT_DECIDED,
    scores: List<hu.blu3berry.avalon.core.domain.model.Score> = emptyList(),
    currentRound: Int = 0,
    isAdventure: Boolean = false,
    currentAdventure: Int = 0,
    king: String? = null,
    selectedForAdventure: List<String> = emptyList(),
    players: List<String> = emptyList(),
    assassinHasGuessed: Boolean = false,
    playerSelectNum: Int = 0,
) = GameInfo(
    started = started,
    winner = winner,
    scores = scores,
    currentRound = currentRound,
    isAdventure = isAdventure,
    currentAdventure = currentAdventure,
    king = king,
    failCounter = 0,
    selectedForAdventure = selectedForAdventure,
    players = players,
    assassinHasGuessed = assassinHasGuessed,
    playerSelectNum = playerSelectNum,
)

class FakeLobbyRepository : LobbyRepository {
    var createResult: Result<String, DataError.Network> = Result.Success("ABCD")
    var joinResult: EmptyResult<DataError.Network> = Result.Success(Unit)
    var leaveResult: EmptyResult<DataError.Network> = Result.Success(Unit)
    var startResult: EmptyResult<DataError.Network> = Result.Success(Unit)
    var settingsResult: Result<LobbySettings, DataError.Network> = Result.Success(defaultSettings)
    var updateSettingsResult: EmptyResult<DataError.Network> = Result.Success(Unit)
    var playerNamesResults = ArrayDeque<Result<List<String>, DataError.Network>>()
    var playerNamesCalls = 0
    var updatedSettings: LobbySettings? = null

    override suspend fun create() = createResult
    override suspend fun join(lobbyCode: String) = joinResult
    override suspend fun leave(lobbyCode: String) = leaveResult
    override suspend fun start(lobbyCode: String) = startResult

    override suspend fun getSettings(lobbyCode: String) = settingsResult

    override suspend fun updateSettings(
        lobbyCode: String,
        settings: LobbySettings,
    ): EmptyResult<DataError.Network> {
        updatedSettings = settings
        return updateSettingsResult
    }

    override suspend fun getPlayerNames(lobbyCode: String): Result<List<String>, DataError.Network> {
        playerNamesCalls++
        return playerNamesResults.removeFirstOrNull() ?: Result.Success(listOf("arthur"))
    }
}

class FakeGameRepository : GameRepository {
    var gameInfoResult: Result<GameInfo, DataError.Network> = Result.Success(gameInfo(started = false))
    var characterResult: Result<Character, DataError.Network> = Result.Failure(DataError.Network.NOT_FOUND)

    /** Hand-driven feed for [observeGameInfo]; tests emit into it. */
    val infoFlow = MutableSharedFlow<Result<GameInfo, DataError.Network>>()

    var selectForAdventureArgs: List<String>? = null
    var voteOnTeamArgs: Pair<String, Boolean>? = null
    var voteOnAdventureArgs: Pair<String, Boolean>? = null
    var merlinGuess: String? = null

    override fun observeGameInfo(lobbyCode: String): Flow<Result<GameInfo, DataError.Network>> = infoFlow

    override suspend fun getGameInfo(lobbyCode: String) = gameInfoResult

    override suspend fun getCharacter(lobbyCode: String): Result<Character, DataError.Network> =
        characterResult

    override suspend fun voteOnTeam(lobbyCode: String, username: String, approve: Boolean): EmptyResult<DataError.Network> {
        voteOnTeamArgs = username to approve
        return Result.Success(Unit)
    }

    override suspend fun voteOnAdventure(lobbyCode: String, username: String, succeed: Boolean): EmptyResult<DataError.Network> {
        voteOnAdventureArgs = username to succeed
        return Result.Success(Unit)
    }

    override suspend fun selectForAdventure(lobbyCode: String, players: List<String>): EmptyResult<DataError.Network> {
        selectForAdventureArgs = players
        return Result.Success(Unit)
    }

    override suspend fun guessMerlin(lobbyCode: String, username: String): EmptyResult<DataError.Network> {
        merlinGuess = username
        return Result.Success(Unit)
    }
}
