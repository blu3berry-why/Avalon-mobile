package hu.blu3berry.avalon.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Avalon-blue seed palette; everything else stays Material defaults until a real
// design pass happens.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2D4E8A),
    secondary = Color(0xFF6B4E8A),
    tertiary = Color(0xFF8A6B2D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C3F5),
    secondary = Color(0xFFCDB8E8),
    tertiary = Color(0xFFE8D0A0),
)

@Composable
fun AvalonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
