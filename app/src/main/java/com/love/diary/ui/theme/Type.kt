package com.love.diary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 适配浪漫日记风格的排版（保留核心结构，优化细节）
val Typography = Typography(
    // 顶级标题（比如App名称/日记封面标题）：弱化字重+微增字间距，更浪漫
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold, // 原 Bold → SemiBold，降低厚重感
        fontSize = 32.sp,
        lineHeight = 42.sp, // 原 40.sp → 增加2sp，呼吸感更强
        letterSpacing = 0.5.sp // 原 0.sp → 微增字间距，复古感更浓
    ),
    // 页面主标题（比如单篇日记标题）
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold, // 原 Bold → SemiBold，更柔和
        fontSize = 24.sp,
        lineHeight = 34.sp, // 原 32.sp → 增加2sp
        letterSpacing = 0.3.sp // 微增字间距
    ),
    // 卡片/模块标题（比如「我的日记」「收藏夹」）
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold, // 保持 SemiBold，和 headline 区分
        fontSize = 22.sp,
        lineHeight = 29.sp, // 原 28.sp → 微调
        letterSpacing = 0.2.sp
    ),
    // 次级标题（比如日记日期/分类标签）
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium, // 原 SemiBold → Medium，弱化次要标题
        fontSize = 16.sp,
        lineHeight = 24.sp, // 保持不变
        letterSpacing = 0.sp
    ),
    // 小型标题（比如按钮文字/输入框提示）
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp, // 保持不变
        letterSpacing = 0.sp
    ),
    // 日记正文（核心阅读区）：微调行高，提升阅读舒适度
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp, // 原 24.sp → 增加2sp，长时间阅读不拥挤
        letterSpacing = 0.sp
    ),
    // 次要正文（比如日记备注/补充说明）
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp, // 原 20.sp → 微调
        letterSpacing = 0.sp
    ),
    // 辅助文字（比如时间戳/底部说明）
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light, // 原 Normal → Light，更轻盈
        fontSize = 12.sp,
        lineHeight = 17.sp, // 原 16.sp → 微调
        letterSpacing = 0.sp
    ),
    // 按钮/标签文字（主要交互元素）
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp, // 保持不变
        letterSpacing = 0.sp
    ),
    // 小型标签/提示文字（比如输入框占位符）
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp, // 保持不变
        letterSpacing = 0.sp
    )
)