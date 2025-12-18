package com.love.diary.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

// 打卡事项类型枚举
enum class HabitType {
    POSITIVE,    // 正向打卡（增加天数）
    COUNTDOWN    // 倒计时（减少天数）
}

// 正向打卡展示类型枚举
enum class PositiveDisplayType {
    WEEKLY,    // 周展示
    MONTHLY    // 月展示
}

// 打卡事项实体
@Entity(
    tableName = "habits",
    indices = [Index(value = ["isActive"], name = "idx_habits_active")]
)
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val buttonLabel: String = "打卡",
    val type: HabitType = HabitType.POSITIVE,
    val displayType: PositiveDisplayType = PositiveDisplayType.WEEKLY, // 展示类型，默认为周展示
    val targetDate: String? = null, // 用于倒计时类型的截止日期
    val startDate: String = LocalDate.now().toString(),
    val currentCount: Int = 0,
    val longestStreak: Int = 0,      // 最长连续天数
    val currentStreak: Int = 0,      // 当前连续天数
    val totalCheckIns: Int = 0,      // 累计打卡次数
    val isCompletedToday: Boolean = false,
    val isActive: Boolean = true,
    val color: String = "#6200EE", // 默认主题色
    val icon: String = "🎯", // 默认图标
    val tags: String = "", // 逗号分隔的标签列表
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 打卡记录实体
@Entity(
    tableName = "habit_records",
    indices = [Index(value = ["habitId", "date"], name = "idx_habit_records_habit_date")]
)
data class HabitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    val date: String = LocalDate.now().toString(),
    val count: Int, // 打卡后的累计次数
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)