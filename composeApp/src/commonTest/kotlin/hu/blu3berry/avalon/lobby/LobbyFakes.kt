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
import kotlinx.coroutines.flow.flowOf

val defaultSettings = LobbySettings(
    assassin = true,
    mordred = false,
    morgana = false,
    oberon = false,
    percival = false,
    arnold = false,
)

fun gameInfo(started: Boolean) = GameInfo(
    started = started,
    winner = Winner.NOT_DECIDED,
    scores = emptyList(),
    currentRound = 0,
    isAdventure = false,
    currentAdventure = 0,
    king = null,
    failCounter = 0,
    selectedForAdventure = emptyList(),
    players = emptyList(),
    assassinHasGuessed = false,
    playerSelectNum = 0,
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

    override fun observeGameInfo(lobbyCode: String): Flow<Result<GameInfo, DataError.Network>> =
        flowOf(gameInfoResult)

    override suspend fun getGameInfo(lobbyCode: String) = gameInfoResult

    override suspend fun getCharacter(lobbyCode: String): Result<Character, DataError.Network> =
        Result.Failure(DataError.Network.NOT_FOUND)

    override suspend fun voteOnTeam(lobbyCode: String, username: String, approve: Boolean): EmptyResult<DataError.Network> =
        Result.Success(Unit)

    override suspend fun voteOnAdventure(lobbyCode: String, username: String, succeed: Boolean): EmptyResult<DataError.Network> =
        Result.Success(Unit)

    override suspend fun selectForAdventure(lobbyCode: String, players: List<String>): EmptyResult<DataError.Network> =
        Result.Success(Unit)

    override suspend fun guessMerlin(lobbyCode: String, username: String): EmptyResult<DataError.Network> =
        Result.Success(Unit)
}
