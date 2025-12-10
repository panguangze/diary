package com.love.diary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

// 统一的打卡类型枚举
enum class CheckInType {
    LOVE_DIARY,    // 异地恋日记（使用心情类型）
    HABIT          // 普通打卡事项
}

// 统一的打卡实体
@Entity(tableName = "checkins")
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,                    // 打卡名称
    val type: CheckInType,               // 打卡类型
    val moodType: MoodType? = null,      // 心情类型（仅用于异地恋日记）
    val habitId: Long? = null,           // 习惯ID（仅用于普通打卡）
    val tag: String? = null,             // 标签（用于普通打卡的备注）
    val date: String = LocalDate.now().toString(),  // 打卡日期
    val count: Int = 0,                  // 计数
    val createdAt: Long = System.currentTimeMillis()
)

// 打卡配置实体 - 用于存储各种打卡事项的配置
@Entity(tableName = "checkin_configs")
data class CheckInConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,                    // 打卡名称
    val type: CheckInType,               // 打卡类型
    val description: String? = null,     // 描述
    val buttonLabel: String = "打卡",     // 按钮标签
    val targetDate: String? = null,      // 目标日期（用于倒计时）
    val startDate: String = LocalDate.now().toString(), // 开始日期
    val icon: String = "🎯",              // 图标
    val color: String = "#6200EE",       // 颜色
    val isActive: Boolean = true,        // 是否激活
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)