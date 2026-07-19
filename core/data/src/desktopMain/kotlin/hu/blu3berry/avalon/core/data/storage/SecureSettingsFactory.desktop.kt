package hu.blu3berry.avalon.core.data.storage

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

// Uses the JVM Preferences API (macOS: ~/Library/Preferences, Linux: ~/.java/.userPrefs,
// Windows: HKCU registry). Not OS-keyring backed — fine for dev/CI desktop builds;
// revisit before any non-dev desktop distribution.
actual class SecureSettingsFactory {
    actual fun create(): Settings =
        PreferencesSettings(Preferences.userRoot().node(PREFS_NODE))

    private companion object {
        const val PREFS_NODE = "hu/blu3berry/avalon/tokens"
    }
}
