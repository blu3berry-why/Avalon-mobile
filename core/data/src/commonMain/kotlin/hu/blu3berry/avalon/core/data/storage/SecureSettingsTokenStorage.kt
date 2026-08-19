package hu.blu3berry.avalon.core.data.storage

import com.russhwolf.settings.Settings

/** [TokenStorage] backed by an encrypted [Settings] (EncryptedSharedPreferences on Android,
 *  Keychain on iOS, JVM Preferences on desktop — see [SecureSettingsFactory]). */
class SecureSettingsTokenStorage(private val settings: Settings) : TokenStorage {
    override suspend fun hasToken(): Boolean = settings.hasKey(KEY_AUTH_TOKEN)
    override suspend fun getToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)
    override suspend fun saveToken(token: String) { settings.putString(KEY_AUTH_TOKEN, token) }
    override suspend fun getUsername(): String? = settings.getStringOrNull(KEY_USERNAME)
    override suspend fun saveUsername(username: String) { settings.putString(KEY_USERNAME, username) }
    override suspend fun clear() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USERNAME)
    }

    private companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_USERNAME = "auth_username"
    }
}
