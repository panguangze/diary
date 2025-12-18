package com.love.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===================== 浪漫日记风格配色（iOS 17 轻盈质感+粉调浪漫） =====================
private val RomanticLightColorScheme = lightColorScheme(
    // 主色：低饱和玫瑰粉（浪漫核心，不刺眼），替代原冷蓝
    primary = Color(0xFFE86E94),
    onPrimary = Color(0xFFFFFFFF),        // 纯白文字，对比清晰
    // 主容器：极浅的粉调白，通透柔和（日记卡片/按钮背景）
    primaryContainer = Color(0xFFFFEEF2),
    onPrimaryContainer = Color(0xFF832845), // 深豆沙色文字，和浅粉容器对比适中

    // 次要色：奶油杏色（暖调，替代原冷灰，增加温馨感）
    secondary = Color(0xFFF0B888),
    onSecondary = Color(0xFFFFFFFF),      // 纯白文字
    // 次要容器：浅奶油白（标签/次要按钮背景）
    secondaryContainer = Color(0xFFFFF5EB),
    onSecondaryContainer = Color(0xFF7A4F28), // 浅棕文字，暖调和谐

    // 三阶色：暖调浅灰（分割线/次要图标，避免冷硬）
    tertiary = Color(0xFF9D8E84),
    onTertiary = Color(0xFFFFFFFF),
    // 三阶容器：极浅的暖白（输入框/卡片边框内背景）
    tertiaryContainer = Color(0xFFFAF6F2),
    onTertiaryContainer = Color(0xFF4A3F38),

    // 背景：纸质感暖白（日记本纸张的感觉，比纯白更温柔）
    background = Color(0xFFFCFAF8),
    // 表面：纯白（核心卡片/内容区，和背景形成微弱层级）
    surface = Color(0xFFFFFFFF),
    // 表面变体：极浅的暖灰（工具栏/侧边栏）
    surfaceVariant = Color(0xFFF7F3EF),
    // 文字：暖调深灰（避免纯黑刺眼，保留浪漫柔和感）
    onBackground = Color(0xFF4A3F38),
    onSurface = Color(0xFF4A3F38),
    // 次要文字：暖调浅灰（时间/备注，易读不抢戏）
    onSurfaceVariant = Color(0xFF9D8E84),

    // 轮廓线：极浅的暖灰（分割线不生硬）
    outline = Color(0xFFE8E0D8),
    outlineVariant = Color(0xFFF0E8E0),

    // 错误色：低饱和红（和整体粉调协调，不突兀）
    error = Color(0xFFE85A6F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE8EC),
    onErrorContainer = Color(0xFF832838)
)

private val RomanticDarkColorScheme = darkColorScheme(
    // 深色模式保留浪漫感，低饱和暗粉+深棕灰
    primary = Color(0xFFF08DA8),
    onPrimary = Color(0xFF832845),
    primaryContainer = Color(0xFF832845),
    onPrimaryContainer = Color(0xFFFFEEF2),

    secondary = Color(0xFFF4C898),
    onSecondary = Color(0xFF7A4F28),
    secondaryContainer = Color(0xFF7A4F28),
    onSecondaryContainer = Color(0xFFFFF5EB),

    tertiary = Color(0xFFBFAF9F),
    onTertiary = Color(0xFF4A3F38),
    tertiaryContainer = Color(0xFF4A3F38),
    onTertiaryContainer = Color(0xFFFAF6F2),

    background = Color(0xFF2A2522),
    surface = Color(0xFF332D29),
    surfaceVariant = Color(0xFF403832),
    onBackground = Color(0xFFF0E8E0),
    onSurface = Color(0xFFF0E8E0),
    onSurfaceVariant = Color(0xFFBFAF9F),

    outline = Color(0xFF6A5F58),
    outlineVariant = Color(0xFF403832),

    error = Color(0xFFF87085),
    onError = Color(0xFF832838),
    errorContainer = Color(0xFF832838),
    onErrorContainer = Color(0xFFFFE8EC)
)

@Composable
fun LoveDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RomanticDarkColorScheme else RomanticLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}