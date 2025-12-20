package com.love.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===================== 浪漫日记风格配色（优化版 - 更精致的色彩层次） =====================
private val RomanticLightColorScheme = lightColorScheme(
    // 主色：优雅玫瑰粉（更饱和但不刺眼）
    primary = Color(0xFFE5718E),
    onPrimary = Color(0xFFFFFFFF),        // 纯白文字，对比清晰
    // 主容器：极浅粉白，更通透柔和
    primaryContainer = Color(0xFFFFF0F4),
    onPrimaryContainer = Color(0xFF8B2D4F), // 更深的豆沙色，增强对比

    // 次要色：温暖杏色（提升温馨感）
    secondary = Color(0xFFEEB07A),
    onSecondary = Color(0xFFFFFFFF),      // 纯白文字
    // 次要容器：浅奶油白
    secondaryContainer = Color(0xFFFFF7ED),
    onSecondaryContainer = Color(0xFF6D4528), // 更深的棕色，更好的对比

    // 三阶色：精致灰调（更平衡的中性色）
    tertiary = Color(0xFF9A8A80),
    onTertiary = Color(0xFFFFFFFF),
    // 三阶容器：极浅暖白
    tertiaryContainer = Color(0xFFFBF8F5),
    onTertiaryContainer = Color(0xFF3D3530),

    // 背景：高级象牙白（更纯净但保留温度）
    background = Color(0xFFFDFBFA),
    // 表面：纯白（卡片/内容区）
    surface = Color(0xFFFFFFFF),
    // 表面变体：极浅暖灰（更精致的层级区分）
    surfaceVariant = Color(0xFFF9F6F3),
    // 文字：深灰（提升可读性，保持柔和）
    onBackground = Color(0xFF2D2824),
    onSurface = Color(0xFF2D2824),
    // 次要文字：优雅中灰（更清晰但不抢戏）
    onSurfaceVariant = Color(0xFF847B72),

    // 轮廓线：更精致的浅灰（subtle但清晰）
    outline = Color(0xFFE5DED6),
    outlineVariant = Color(0xFFF0EBE5),

    // 错误色：优雅的低饱和红
    error = Color(0xFFE65D72),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF8B2935)
)

private val RomanticDarkColorScheme = darkColorScheme(
    // 深色模式 - 优化对比度和现代感
    primary = Color(0xFFED90AA),
    onPrimary = Color(0xFF5D1D33),
    primaryContainer = Color(0xFF7D2E46),
    onPrimaryContainer = Color(0xFFFFF0F4),

    secondary = Color(0xFFF1C598),
    onSecondary = Color(0xFF5A3A1F),
    secondaryContainer = Color(0xFF6F4A2C),
    onSecondaryContainer = Color(0xFFFFF7ED),

    tertiary = Color(0xFFC0AEA0),
    onTertiary = Color(0xFF332B25),
    tertiaryContainer = Color(0xFF4A3E38),
    onTertiaryContainer = Color(0xFFFBF8F5),

    background = Color(0xFF151515),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2A2A2A),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFFB8B0A8),

    outline = Color(0xFF3D3D3D),
    outlineVariant = Color(0xFF2A2A2A),

    error = Color(0xFFF9758C),
    onError = Color(0xFF5D1D2A),
    errorContainer = Color(0xFF7D2E3D),
    onErrorContainer = Color(0xFFFFEBEE)
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