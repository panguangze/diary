package com.love.diary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 统一的事件类型枚举
 */
enum class EventType {
    MOOD_DIARY,    // 心情日记
    HABIT_CHECK_IN // 习惯打卡
}

/**
 * 通用事件实体 - 替代原来的CheckIn
 */
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,                    // 事件名称
    val type: EventType,                 // 事件类型
    val moodType: MoodType? = null,      // 心情类型（仅用于心情日记）
    val habitId: Long? = null,           // 习惯ID（仅用于习惯打卡）
    val tag: String? = null,             // 标签（用于备注）
    val date: String = LocalDate.now().toString(),  // 事件日期
    val count: Int = 0,                  // 计数
    val note: String? = null,            // 备注
    val metadata: String? = null,        // 额外元数据（JSON格式）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 事件配置实体 - 用于存储各种事件的配置
 */
@Entity(tableName = "event_configs")
data class EventConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,                    // 事件名称
    val type: EventType,                 // 事件类型
    val description: String? = null,     // 描述
    val buttonLabel: String = "记录",     // 按钮标签
    val targetDate: String? = null,      // 目标日期（用于倒计时）
    val startDate: String = LocalDate.now().toString(), // 开始日期
    val icon: String = "📝",              // 图标
    val color: String = "#6200EE",       // 颜色
    val isActive: Boolean = true,        // 是否激活
    val metadata: String? = null,        // 额外元数据（JSON格式）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)