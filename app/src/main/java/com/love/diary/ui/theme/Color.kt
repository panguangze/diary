package com.love.diary.ui.theme

import androidx.compose.ui.graphics.Color

/// ===================== 高级白色系 - Light 模式基础色调（无棕调） =====================
// 背景/表面：不同层级的高级白（柔白/亮白/浅灰白，极简高级感）
public val WarmLightBackground = Color(0xFFFAFAFA)   // 高级柔白（替代原暖棕背景）
public val WarmLightSurface = Color(0xFFFFFFFF)      // 纯净亮白（核心交互区域）
public val WarmLightSurfaceVariant = Color(0xFFF5F5F5)// 浅灰白（表面变体，层级区分）

// 文字/内容色：低饱和深灰（避免纯黑，更显高级）
public val WarmLightOnSurface = Color(0xFF121212)    // 深灰（替代原棕系文字）
public val WarmLightOnSurfaceVariant = Color(0xFF616161)// 中灰（次要文字）

// 轮廓色：极浅灰（弱化边框，不破坏白色基调）
public val WarmLightOutline = Color(0xFFE0E0E0)      // 极浅灰轮廓
public val WarmLightOutlineVariant = Color(0xFFEEEEEE)// 超浅灰轮廓变体

// ===================== 错误色定义（保留原有，适配双模式） =====================
public val LightErrorContainer = Color(0xFFFFDAD6)
public val OnLightErrorContainer = Color(0xFF410002)
public val DarkErrorContainer = Color(0xFF8C1D3F)
public val OnDarkErrorContainer = Color(0xFFFFD9E3)

// ===================== Dark 模式基础色调（完全保留原有配色） =====================
public val WarmDarkBackground = Color(0xFF1A1115)
public val WarmDarkSurface = Color(0xFF23171C)
public val WarmDarkSurfaceVariant = Color(0xFF3A2A30)
public val WarmDarkOnSurface = Color(0xFFF1E6EA)
public val WarmDarkOnSurfaceVariant = Color(0xFFD0C1C8)
public val WarmDarkOutline = Color(0xFF6A555E)
public val WarmDarkOutlineVariant = Color(0xFF8A737C)

// ===================== Tag Colors Schema（标签颜色方案） =====================
// 用于打卡标签的5种配色，按顺序自动分配
public val TagColor1 = Color(0xFF6200EE)  // 紫色
public val TagColor2 = Color(0xFF03DAC6)  // 青色
public val TagColor3 = Color(0xFFFF6F00)  // 橙色
public val TagColor4 = Color(0xFFE91E63)  // 粉色
public val TagColor5 = Color(0xFF4CAF50)  // 绿色
public val TagColorOther = Color(0xFF9E9E9E) // 灰色（"其它"标签专用）

// Tag color list for easy access
public val TagColors = listOf(
    TagColor1,
    TagColor2,
    TagColor3,
    TagColor4,
    TagColor5
)
