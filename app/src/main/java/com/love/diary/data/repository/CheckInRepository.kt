package com.love.diary.data.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.model.CheckIn
import com.love.diary.data.model.CheckInConfig
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.CheckInTrend
import com.love.diary.data.model.MoodType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class CheckInRepository @Inject constructor(
    private val database: LoveDatabase
) {
    private val checkInDao = database.checkInDao()

    // 获取所有打卡配置
    fun getAllCheckInConfigs(): Flow<List<CheckInConfig>> {
        return checkInDao.getAllCheckInConfigs()
    }

    // 根据类型获取打卡配置
    fun getCheckInConfigsByType(type: CheckInType): Flow<List<CheckInConfig>> {
        return checkInDao.getCheckInConfigsByType(type)
    }

    // 根据ID获取打卡配置
    suspend fun getCheckInConfigById(id: Long): CheckInConfig? {
        return checkInDao.getCheckInConfigById(id)
    }

    // 根据名称获取打卡配置
    suspend fun getCheckInConfigByName(name: String): CheckInConfig? {
        return checkInDao.getCheckInConfigByName(name)
    }

    // 创建或更新打卡配置
    suspend fun saveCheckInConfig(config: CheckInConfig): Long {
        return checkInDao.insertCheckInConfig(config)
    }

    // 更新打卡配置
    suspend fun updateCheckInConfig(config: CheckInConfig) {
        checkInDao.updateCheckInConfig(config)
    }

    // 删除打卡配置（软删除）
    suspend fun deleteCheckInConfig(id: Long) {
        checkInDao.deactivateCheckInConfig(id)
    }

    // 通用打卡功能
    suspend fun checkIn(
        name: String,
        type: CheckInType,
        moodType: MoodType? = null,
        tag: String? = null,
        note: String? = null,
        attachmentUri: String? = null,
        duration: Int? = null,
        rating: Int? = null,
        count: Int = 1
    ): Long {
        val config = getCheckInConfigByName(name)
        if (config == null) {
            // 如果配置不存在，创建一个默认配置
            val newConfig = CheckInConfig(
                name = name,
                type = type,
                description = when(type) {
                    CheckInType.LOVE_DIARY -> "恋爱时间记录"
                    CheckInType.HABIT -> "打卡事项"
                    CheckInType.MILESTONE -> "里程碑事件"
                    CheckInType.DAILY_TASK -> "日常任务"
                },
                buttonLabel = when(type) {
                    CheckInType.LOVE_DIARY -> "记录恋爱时光"
                    CheckInType.HABIT -> "打卡"
                    CheckInType.MILESTONE -> "记录里程碑"
                    CheckInType.DAILY_TASK -> "完成任务"
                },
                icon = when(type) {
                    CheckInType.LOVE_DIARY -> "❤️"
                    CheckInType.HABIT -> "✅"
                    CheckInType.MILESTONE -> "🏆"
                    CheckInType.DAILY_TASK -> "📝"
                }
            )
            saveCheckInConfig(newConfig)
        }

        val today = LocalDate.now().toString()
        val todayCheckIn = checkInDao.getCheckInByDateAndName(today, name)
        
        // 如果今天已经打卡，先删除旧记录
        if (todayCheckIn != null) {
            checkInDao.deleteCheckInById(todayCheckIn.id)
        }

        val checkIn = CheckIn(
            name = name,
            type = type,
            moodType = moodType,
            tag = tag,
            date = today,
            count = count,
            note = note,
            attachmentUri = attachmentUri,
            duration = duration,
            rating = rating,
            isCompleted = true
        )

        return checkInDao.insertCheckIn(checkIn)
    }

    // 打卡 - 恋爱时间记录类型（特殊打卡）
    suspend fun checkInLoveDiary(
        name: String = "恋爱日记",
        moodType: MoodType,
        note: String? = null,
        attachmentUri: String? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.LOVE_DIARY,
            moodType = moodType,
            note = note,
            attachmentUri = attachmentUri
        )
    }

    // 打卡 - 普通习惯类型
    suspend fun checkInHabit(
        name: String,
        tag: String? = null,
        note: String? = null,
        attachmentUri: String? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.HABIT,
            tag = tag,
            note = note,
            attachmentUri = attachmentUri
        )
    }

    // 打卡 - 里程碑事件
    suspend fun checkInMilestone(
        name: String,
        note: String? = null,
        attachmentUri: String? = null,
        rating: Int? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.MILESTONE,
            note = note,
            attachmentUri = attachmentUri,
            rating = rating
        )
    }

    // 打卡 - 日常任务
    suspend fun checkInDailyTask(
        name: String,
        note: String? = null,
        duration: Int? = null,
        isCompleted: Boolean = true
    ): Long {
        val today = LocalDate.now().toString()
        val todayCheckIn = checkInDao.getCheckInByDateAndName(today, name)
        
        // 如果今天已经打卡，先删除旧记录
        if (todayCheckIn != null) {
            checkInDao.deleteCheckInById(todayCheckIn.id)
        }

        val checkIn = CheckIn(
            name = name,
            type = CheckInType.DAILY_TASK,
            tag = note,
            date = today,
            count = if (isCompleted) 1 else 0,
            note = note,
            duration = duration,
            isCompleted = isCompleted
        )

        return checkInDao.insertCheckIn(checkIn)
    }

    // 获取特定打卡事项的记录
    fun getCheckInsByName(name: String): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByName(name)
    }

    // 获取特定日期的打卡记录
    fun getCheckInsByDate(date: String): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByDate(date)
    }

    // 获取指定日期范围内的打卡记录
    fun getCheckInsBetweenDates(startDate: String, endDate: String): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsBetweenDates(startDate, endDate)
    }

    // 获取特定类型的打卡记录
    fun getCheckInsByType(type: CheckInType): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByType(type)
    }

    // 获取特定类型和日期范围内的打卡记录
    fun getCheckInsByTypeAndDateRange(type: CheckInType, startDate: String, endDate: String): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByTypeAndDateRange(type, startDate, endDate)
    }

    // 获取所有唯一的打卡类型
    fun getUniqueCheckInTypes(): Flow<List<CheckInType>> {
        return checkInDao.getUniqueCheckInTypes()
    }

    // 获取打卡统计
    suspend fun getCheckInCountByName(name: String): Int {
        return checkInDao.getCheckInCountByName(name)
    }

    // 获取最近的打卡记录
    suspend fun getRecentCheckInsByName(name: String, limit: Int): List<CheckIn> {
        return checkInDao.getRecentCheckInsByName(name, limit)
    }

    // 获取打卡趋势
    suspend fun getCheckInTrendByName(name: String): List<CheckInTrend> {
        return checkInDao.getCheckInTrendByName(name)
    }

    // 获取恋爱日记记录
    fun getLoveDiaryRecords(): Flow<List<CheckIn>> {
        return checkInDao.getLoveDiaryRecords()
    }

    // 获取指定日期范围内的恋爱日记记录
    suspend fun getLoveDiaryRecordsBetweenDates(startDate: String, endDate: String): List<CheckIn> {
        return checkInDao.getLoveDiaryRecordsBetweenDates(startDate, endDate)
    }

    // 获取最新的恋爱日记记录
    suspend fun getLatestLoveDiaryRecord(): CheckIn? {
        return checkInDao.getLatestLoveDiaryRecord()
    }

    // 批量插入打卡记录
    suspend fun insertCheckIns(checkIns: List<CheckIn>): List<Long> {
        return checkInDao.insertCheckIns(checkIns)
    }

    // 更新打卡记录
    suspend fun updateCheckIn(checkIn: CheckIn) {
        checkInDao.updateCheckIn(checkIn)
    }

    // 删除打卡记录
    suspend fun deleteCheckIn(id: Long) {
        checkInDao.deleteCheckInById(id)
    }
}