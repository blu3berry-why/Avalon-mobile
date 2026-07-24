package hu.blu3berry.avalon.core.data.storage

interface TokenStorage {
    suspend fun hasToken(): Boolean
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clear()
}
