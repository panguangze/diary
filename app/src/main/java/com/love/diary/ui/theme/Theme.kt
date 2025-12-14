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
    secondary = Color(0xFFFF80AB),
    tertiary = Color(0xFFF48FB1),
    background = Color(0xFFFFF9F9),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF333333),
    onSurface = Color(0xFF333333),
    error = Color(0xFFF44336),
    primaryContainer = Color(0xFFFFE0E9)
)

/**
 * Dark theme color scheme with softer pink tones for night use
 */
private val LoveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF80AB),
    secondary = Color(0xFFF48FB1),
    tertiary = Color(0xFFF06292),
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2D2D2D),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
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
        typography = MaterialTheme.typography,
        content = content
    )
}
