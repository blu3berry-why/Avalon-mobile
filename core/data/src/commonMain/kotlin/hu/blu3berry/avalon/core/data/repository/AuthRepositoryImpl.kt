package hu.blu3berry.avalon.core.data.repository

import hu.blu3berry.avalon.core.data.generated.auth.api.DefaultApi
import hu.blu3berry.avalon.core.data.generated.auth.models.Credentials
import hu.blu3berry.avalon.core.data.generated.auth.models.Registration
import hu.blu3berry.avalon.core.data.network.toResult
import hu.blu3berry.avalon.core.data.storage.TokenStorage
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.asEmptyDataResult
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.core.domain.session.AuthEvent
import hu.blu3berry.avalon.core.domain.session.SessionManager

/**
 * Talks to the ForwardAuth service, which is a separate deployment from the game API — hence
 * the separate generated `auth` package with its own `Api` base URL.
 *
 * No `emitLogoutOnUnauthorized` here: a 401 from /login is "wrong password", not an expired
 * session, and firing a logout event on it would be wrong.
 */
class AuthRepositoryImpl(
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override suspend fun login(username: String, password: String): EmptyResult<DataError.Network> =
        DefaultApi.login(Credentials(username = username, password = password))
            .toResult()
            .onSuccess { token -> tokenStorage.saveToken(token.token) }
            .asEmptyDataResult()

    override suspend fun register(
        username: String,
        password: String,
        email: String?,
    ): EmptyResult<DataError.Network> =
        DefaultApi.register(Registration(username = username, password = password, email = email))
            .toResult()
            .asEmptyDataResult()

    override suspend fun logout() {
        tokenStorage.clear()
        sessionManager.emit(AuthEvent.LogoutRequired)
    }

    override suspend fun isLoggedIn(): Boolean = tokenStorage.hasToken()
}
