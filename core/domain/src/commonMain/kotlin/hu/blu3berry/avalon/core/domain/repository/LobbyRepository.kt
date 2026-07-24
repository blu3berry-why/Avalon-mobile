package hu.blu3berry.avalon.core.domain.repository

import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result

/**
 * Pre-game lobby: create/join/leave, role settings, and starting the game. The server
 * answers most of these with a free-text `Message` that carries no machine-readable
 * discriminator, so success is reported as [EmptyResult] and the text is dropped.
 */
interface LobbyRepository {

    /** @return the new lobby's join code. */
    suspend fun create(): Result<String, DataError.Network>

    suspend fun join(lobbyCode: String): EmptyResult<DataError.Network>

    suspend fun leave(lobbyCode: String): EmptyResult<DataError.Network>

    suspend fun start(lobbyCode: String): EmptyResult<DataError.Network>

    suspend fun getSettings(lobbyCode: String): Result<LobbySettings, DataError.Network>

    suspend fun updateSettings(
        lobbyCode: String,
        settings: LobbySettings,
    ): EmptyResult<DataError.Network>

    suspend fun getPlayerNames(lobbyCode: String): Result<List<String>, DataError.Network>
}
