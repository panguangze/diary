package com.love.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LoveLightColorScheme = lightColorScheme(
    primary = Color(0xFFB9806E), // rose-gold accent
    onPrimary = Color(0xFF2B120B),
    primaryContainer = Color(0xFFEFE2DA),
    onPrimaryContainer = Color(0xFF3C241B),
    secondary = Color(0xFF6D6A75), // slate accents
    onSecondary = Color.White,
    tertiary = Color(0xFFC3A68A),
    onTertiary = Color(0xFF2D1B0D),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF4F0EB),
    onBackground = Color(0xFF1F1410),
    onSurface = Color(0xFF1F1410),
    onSurfaceVariant = Color(0xFF5C514A),
    outline = Color(0xFFE0D6CE),
    outlineVariant = Color(0xFFF1EAE4),
    error = Color(0xFFE57373),
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = OnLightErrorContainer
)

private val LoveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD8B8A5),
    onPrimary = Color(0xFF2A150E),
    primaryContainer = Color(0xFF4A3024),
    onPrimaryContainer = Color(0xFFF4E6DC),
    secondary = Color(0xFF8E8A96),
    onSecondary = Color(0xFF1B181D),
    tertiary = Color(0xFFBBA07F),
    onTertiary = Color(0xFF201309),
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
