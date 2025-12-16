package com.love.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light theme color scheme with romantic pink/red tones
 */
private val LoveLightColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0E9),
    onPrimaryContainer = Color(0xFF5B0A2B),
    secondary = Color(0xFFFF80AB),
    onSecondary = Color.White,
    tertiary = Color(0xFFF48FB1),
    onTertiary = Color.White,
    background = WarmLightBackground,
    surface = WarmLightSurface,
    surfaceVariant = WarmLightSurfaceVariant,
    onBackground = WarmLightOnSurface,
    onSurface = WarmLightOnSurface,
    onSurfaceVariant = WarmLightOnSurfaceVariant,
    outline = WarmLightOutline,
    outlineVariant = WarmLightOutlineVariant,
    error = Color(0xFFF44336),
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = OnLightErrorContainer
)

/**
 * Dark theme color scheme with softer pink tones for night use
 */
private val LoveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF80AB),
    onPrimary = Color(0xFF300015),
    primaryContainer = Color(0xFF5B0A2B),
    onPrimaryContainer = Color(0xFFFFD9E3),
    secondary = Color(0xFFF48FB1),
    onSecondary = Color(0xFF381320),
    tertiary = Color(0xFFF06292),
    onTertiary = Color(0xFF3C0C21),
    background = WarmDarkBackground,
    surface = WarmDarkSurface,
    surfaceVariant = WarmDarkSurfaceVariant,
    onBackground = WarmDarkOnSurface,
    onSurface = WarmDarkOnSurface,
    onSurfaceVariant = WarmDarkOnSurfaceVariant,
    outline = WarmDarkOutline,
    outlineVariant = WarmDarkOutlineVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF680003),
    errorContainer = DarkErrorContainer,
    onErrorContainer = OnDarkErrorContainer
)

/**
 * Main theme composable for the Love Diary app
 * Supports both light and dark themes
 * 
 * @param darkTheme Whether to use dark theme. Defaults to system setting
 * @param content The content to be themed
 */
@Composable
fun LoveDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LoveDarkColorScheme else LoveLightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
