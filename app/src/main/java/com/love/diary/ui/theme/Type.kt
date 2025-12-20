package com.love.diary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 优化的浪漫日记风格排版系统
val Typography = Typography(
    // 顶级标题（App名称/封面标题）：优雅serif字体，增强呼吸感
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,   // SemiBold → Medium，更轻盈
        fontSize = 32.sp,
        lineHeight = 44.sp,               // 42 → 44sp，更舒适
        letterSpacing = 0.8.sp            // 0.5 → 0.8sp，更优雅
    ),
    // 页面主标题（日记标题）
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,   // SemiBold → Medium
        fontSize = 24.sp,
        lineHeight = 36.sp,               // 34 → 36sp，更宽松
        letterSpacing = 0.5.sp            // 0.3 → 0.5sp
    ),
    // 卡片/模块标题
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,   // 保持Medium，区分感
        fontSize = 22.sp,
        lineHeight = 30.sp,               // 29 → 30sp
        letterSpacing = 0.3.sp            // 0.2 → 0.3sp
    ),
    // 次级标题（日期/标签）
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp            // 0 → 0.1sp，微调
    ),
    // 小型标题（按钮文字）
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp            // 增加可读性
    ),
    // 日记正文（核心阅读区）
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 28.sp,               // 26 → 28sp，更易阅读
        letterSpacing = 0.15.sp           // 0 → 0.15sp，更清晰
    ),
    // 次要正文
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,               // 21 → 22sp
        letterSpacing = 0.1.sp            // 增加间距
    ),
    // 辅助文字（时间戳/说明）
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,   // Light → Normal，更易读
        fontSize = 12.sp,
        lineHeight = 18.sp,               // 17 → 18sp
        letterSpacing = 0.4.sp            // 0 → 0.4sp，小字更需要间距
    ),
    // 按钮/标签文字
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp            // 0 → 0.1sp
    ),
    // 小型标签
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp            // 0 → 0.5sp，标签需要清晰间距
    )
)