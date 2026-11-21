package kz.sitetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F5FA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4F7),
    onPrimaryContainer = Color(0xFF0A2340),
    secondary = Color(0xFF55627A),
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFF7F9FC),
    surfaceVariant = Color(0xFFE6EBF2)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA6C8F2),
    onPrimary = Color(0xFF0A2340),
    primaryContainer = Color(0xFF1B4677),
    onPrimaryContainer = Color(0xFFD6E4F7),
    secondary = Color(0xFFBCC7DA),
    background = Color(0xFF111418),
    surface = Color(0xFF111418),
    surfaceVariant = Color(0xFF1F252D)
)

/** Fixed colours for statuses/results, so "green = good, amber = warning" looks the same in both themes. */
val Good = Color(0xFF2E9D4A)
val Warning = Color(0xFFC77800)

@Composable
fun SiteTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
