package com.love.diary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 统一打卡实体 - 用于支持多种类型的打卡功能
 * 恋爱时间记录作为特殊类型的打卡存在
 */
@Entity(tableName = "unified_checkins")
data class UnifiedCheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // 基础信息
    val name: String,                    // 打卡名称
    val type: CheckInType,               // 打卡类型
    val date: String = LocalDate.now().toString(),  // 打卡日期
    
    // 通用字段
    val moodType: MoodType? = null,      // 心情类型（恋爱日记等场景使用）
    val tag: String? = null,             // 标签（分类或备注）
    val note: String? = null,            // 打卡备注
    val attachmentUri: String? = null,   // 附件URI（图片、音频等）
    
    // 计数相关
    val count: Int = 0,                  // 计数（适用于计数型打卡）
    val duration: Int? = null,           // 持续时间（分钟），适用于某些活动
    
    // 评分相关
    val rating: Int? = null,             // 评分（1-5星）
    val isCompleted: Boolean = true,     // 是否完成（适用于任务类打卡）
    
    // 配置相关
    val configId: Long? = null,          // 关联的配置ID
    
    // 元数据
    val metadata: String? = null,        // 额外元数据（JSON格式）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 统一打卡配置实体
 */
@Entity(tableName = "unified_checkin_configs")
data class UnifiedCheckInConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    // 基础信息
    val name: String,                    // 打卡名称
    val type: CheckInType,               // 打卡类型
    val description: String? = null,     // 描述
    
    // UI相关
    val buttonLabel: String = "打卡",     // 按钮标签
    val icon: String = "🎯",              // 图标
    val color: String = "#6200EE",       // 颜色
    
    // 业务逻辑相关
    val startDate: String = LocalDate.now().toString(), // 开始日期
    val targetDate: String? = null,      // 目标日期（用于倒计时或计划）
    val targetValue: Int? = null,        // 目标值（如连续打卡天数）
    val reminderTime: String? = null,    // 提醒时间（HH:mm格式）
    val isRecurring: Boolean = false,    // 是否重复
    val recurrencePattern: String? = null, // 重复模式（daily, weekly, monthly等）
    
    // 状态
    val isActive: Boolean = true,        // 是否激活
    
    // 元数据
    val metadata: String? = null,        // 额外元数据（JSON格式）
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)