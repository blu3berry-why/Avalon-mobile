package hu.blu3berry.avalon.core.data.storage

import com.russhwolf.settings.Settings

expect class SecureSettingsFactory {
    fun create(): Settings
}
