package hu.blu3berry.avalon.core.data.storage

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

@OptIn(ExperimentalSettingsImplementation::class)
actual class SecureSettingsFactory {
    actual fun create(): Settings = KeychainSettings(service = KEYCHAIN_SERVICE)

    private companion object {
        const val KEYCHAIN_SERVICE = "hu.blu3berry.avalon.tokens"
    }
}
