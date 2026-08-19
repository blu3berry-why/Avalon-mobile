package hu.blu3berry.avalon.core.data.storage

interface TokenStorage {
    suspend fun hasToken(): Boolean
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    /** The username the stored token was issued to; null when logged out. */
    suspend fun getUsername(): String?
    suspend fun saveUsername(username: String)
    suspend fun clear()
}
