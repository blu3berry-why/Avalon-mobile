package hu.blu3berry.avalon.core.data.repository

import hu.blu3berry.avalon.core.data.generated.game.api.UserControllerApi
import hu.blu3berry.avalon.core.data.generated.game.models.LoginInfo
import hu.blu3berry.avalon.core.data.generated.game.models.generated.*
import hu.blu3berry.avalon.core.data.network.toResult
import hu.blu3berry.avalon.core.domain.model.User
import hu.blu3berry.avalon.core.domain.repository.UserRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.session.SessionManager

class UserRepositoryImpl(
    private val sessionManager: SessionManager,
) : UserRepository {

    override suspend fun get(username: String): Result<User, DataError.Network> =
        UserControllerApi.getUserByUsername(username = username)
            .toResult(sessionManager) { it.toDomain() }

    /**
     * `LoginInfo` doubles as the update body; the server treats absent fields as "unchanged",
     * so null arguments are passed straight through. `username` identifies the account for
     * the caller's own bookkeeping — the server takes the identity from the gateway header.
     */
    override suspend fun update(
        username: String,
        password: String?,
        email: String?,
    ): Result<User, DataError.Network> =
        UserControllerApi.updateUserByUsername(
            body = LoginInfo(username = username, password = password, email = email),
        ).toResult(sessionManager) { it.toDomain() }

    override suspend fun delete(): Result<User, DataError.Network> =
        UserControllerApi.deleteUserByUsername()
            .toResult(sessionManager) { it.toDomain() }
}
