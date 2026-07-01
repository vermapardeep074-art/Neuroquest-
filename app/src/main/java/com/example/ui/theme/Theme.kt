package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonMagenta,
    tertiary = NeonPurple,
    background = CosmicDarkBg,
    surface = CosmicDarkSurface,
    onPrimary = CosmicDarkBg,
    onSecondary = IceText,
    onTertiary = IceText,
    onBackground = IceText,
    onSurface = IceText
)

private val LightColorScheme = lightColorScheme(
    primary = CosmicDarkSurface,
    secondary = NeonMagenta,
    tertiary = NeonPurple,
    background = IceText,
    surface = CosmicDarkCard,
    onPrimary = IceText,
    onSecondary = CosmicDarkBg,
    onTertiary = CosmicDarkBg,
    onBackground = CosmicDarkBg,
    onSurface = CosmicDarkBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme by default for premium visual gaming experience
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
