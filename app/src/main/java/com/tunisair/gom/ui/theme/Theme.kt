package com.tunisair.gom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tunisair brand colors
private val TunisairNavy = Color(0xFF003366)
private val TunisairGold = Color(0xFFD4AF37)
private val TunisairWhite = Color(0xFFFFFFFF)
private val TunisairLightBlue = Color(0xFFE6F2FF)

private val DarkColors = darkColorScheme(
    primary = TunisairGold,
    onPrimary = Color.Black,
    primaryContainer = TunisairNavy,
    onPrimaryContainer = TunisairWhite,
    secondary = TunisairLightBlue,
    onSecondary = TunisairNavy,
    secondaryContainer = Color(0xFF1A4A7A),
    onSecondaryContainer = TunisairWhite,
    tertiary = Color(0xFFFFD700),
    onTertiary = Color.Black,
    background = Color(0xFF0D1B2A),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1B2A3A),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A3A4A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColors = lightColorScheme(
    primary = TunisairNavy,
    onPrimary = TunisairWhite,
    primaryContainer = TunisairLightBlue,
    onPrimaryContainer = TunisairNavy,
    secondary = TunisairGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFF3C4),
    onSecondaryContainer = Color(0xFF7A5A00),
    tertiary = Color(0xFF005A9C),
    onTertiary = TunisairWhite,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF1A1A2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFE8EEF5),
    onSurfaceVariant = Color(0xFF4A5568),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun GomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
