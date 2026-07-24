package hu.blu3berry.avalon.core.data.repository

import hu.blu3berry.avalon.core.data.generated.game.api.LobbyControllerApi
import hu.blu3berry.avalon.core.data.generated.game.models.generated.*
import hu.blu3berry.avalon.core.data.network.toResult
import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.core.domain.model.generated.*
import hu.blu3berry.avalon.core.domain.repository.LobbyRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.result.asEmptyDataResult
import hu.blu3berry.avalon.core.domain.session.SessionManager

class LobbyRepositoryImpl(
    private val sessionManager: SessionManager,
) : LobbyRepository {

    override suspend fun create(): Result<String, DataError.Network> =
        LobbyControllerApi.createLobby()
            .toResult(sessionManager) { it.code }

    override suspend fun join(lobbyCode: String): EmptyResult<DataError.Network> =
        LobbyControllerApi.joinLobby(lobbyCode = lobbyCode)
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun leave(lobbyCode: String): EmptyResult<DataError.Network> =
        LobbyControllerApi.leaveLobby(lobbyCode = lobbyCode)
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun start(lobbyCode: String): EmptyResult<DataError.Network> =
        LobbyControllerApi.startLobby(lobbyCode = lobbyCode)
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun getSettings(lobbyCode: String): Result<LobbySettings, DataError.Network> =
        LobbyControllerApi.getLobbySettings(lobbyCode = lobbyCode)
            .toResult(sessionManager) { dto -> dto.toDomain() }

    override suspend fun updateSettings(
        lobbyCode: String,
        settings: LobbySettings,
    ): EmptyResult<DataError.Network> =
        LobbyControllerApi.updateSettings(lobbyCode = lobbyCode, body = settings.toDto())
            .toResult(sessionManager)
            .asEmptyDataResult()

    override suspend fun getPlayerNames(lobbyCode: String): Result<List<String>, DataError.Network> =
        LobbyControllerApi.getPlayerNames(lobbyCode = lobbyCode)
            .toResult(sessionManager)
}
