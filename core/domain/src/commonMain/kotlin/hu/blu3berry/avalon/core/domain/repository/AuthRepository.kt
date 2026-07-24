package hu.blu3berry.avalon.core.domain.repository

import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult

/**
 * Login/registration against the ForwardAuth service. A successful [login] persists the JWT
 * in the token store, which is what every other repository's requests authenticate with —
 * callers get no token back and never handle one.
 */
interface AuthRepository {

    suspend fun login(username: String, password: String): EmptyResult<DataError.Network>

    suspend fun register(
        username: String,
        password: String,
        email: String? = null,
    ): EmptyResult<DataError.Network>

    /** Drops the stored token. Purely local — the JWT stays valid server-side until it expires. */
    suspend fun logout()

    suspend fun isLoggedIn(): Boolean
}
