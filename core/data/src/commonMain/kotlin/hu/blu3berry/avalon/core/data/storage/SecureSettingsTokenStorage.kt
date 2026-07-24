package hu.blu3berry.avalon.core.data.storage

import com.russhwolf.settings.Settings

/** [TokenStorage] backed by an encrypted [Settings] (EncryptedSharedPreferences on Android,
 *  Keychain on iOS, JVM Preferences on desktop — see [SecureSettingsFactory]). */
class SecureSettingsTokenStorage(private val settings: Settings) : TokenStorage {
    override suspend fun hasToken(): Boolean = settings.hasKey(KEY_AUTH_TOKEN)
    override suspend fun getToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)
    override suspend fun saveToken(token: String) { settings.putString(KEY_AUTH_TOKEN, token) }
    override suspend fun clear() { settings.remove(KEY_AUTH_TOKEN) }

    private companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
    }
}
