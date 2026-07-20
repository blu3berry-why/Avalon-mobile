package hu.blu3berry.avalon.core.domain.repository

import hu.blu3berry.avalon.core.domain.model.User
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result

/**
 * Account read/update. [update] and [delete] act on the caller — the gateway derives the
 * identity from the JWT, so they take no username.
 */
interface UserRepository {

    suspend fun get(username: String): Result<User, DataError.Network>

    suspend fun update(
        username: String,
        password: String? = null,
        email: String? = null,
    ): Result<User, DataError.Network>

    suspend fun delete(): Result<User, DataError.Network>
}
