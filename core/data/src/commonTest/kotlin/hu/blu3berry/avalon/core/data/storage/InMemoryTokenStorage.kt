package hu.blu3berry.avalon.core.data.storage

/** Test double for [TokenStorage] — no encryption, no persistence, no platform dependency. */
class InMemoryTokenStorage : TokenStorage {
    private var token: String? = null
    override suspend fun hasToken(): Boolean = token != null
    override suspend fun getToken(): String? = token
    override suspend fun saveToken(token: String) { this.token = token }
    private var username: String? = null
    override suspend fun getUsername(): String? = username
    override suspend fun saveUsername(username: String) { this.username = username }
    override suspend fun clear() { token = null; username = null }
}
