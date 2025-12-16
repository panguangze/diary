package com.love.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Light theme color scheme with modern minimalist design
 */
private val LoveLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),        // 主色调 - 紫色
    onPrimary = Color.White,             // 主色调上的文字
    primaryContainer = Color(0xFFEADDFF), // 主色调容器
    onPrimaryContainer = Color(0xFF21005D), // 主色调容器上的文字
    secondary = Color(0xFF625B71),       // 次要色调
    onSecondary = Color.White,           // 次要色调上的文字
    tertiary = Color(0xFF7D5260),        // 第三色调
    onTertiary = Color.White,            // 第三色调上的文字
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = OnLightErrorContainer
)

/**
 * Dark theme color scheme with modern minimalist design
 */
private val LoveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),         // 主色调 - 浅紫色
    onPrimary = Color(0xFF381E72),       // 主色调上的文字
    primaryContainer = Color(0xFF4F378B), // 主色调容器
    onPrimaryContainer = Color(0xFFEADDFF), // 主色调容器上的文字
    secondary = Color(0xFFCCC2DC),       // 次要色调
    onSecondary = Color(0xFF332D41),     // 次要色调上的文字
    tertiary = Color(0xFFEFB8C8),        // 第三色调
    onTertiary = Color(0xFF492532),      // 第三色调上的文字
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
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
