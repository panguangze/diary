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

    // 打卡 - 异地恋日记类型
    suspend fun checkInLoveDiary(name: String, moodType: MoodType): Long {
        val config = getCheckInConfigByName(name)
        if (config == null) {
            // 如果配置不存在，创建一个默认配置
            val newConfig = CheckInConfig(
                name = name,
                type = CheckInType.LOVE_DIARY,
                description = "异地恋日记",
                buttonLabel = "记录心情",
                icon = "❤️"
            )
            saveCheckInConfig(newConfig)
        }

        val today = LocalDate.now().toString()
        val todayCheckIn = checkInDao.getCheckInByDateAndName(today, name)
        
        // 如果今天已经打卡，先删除旧记录
        if (todayCheckIn != null) {
            checkInDao.deleteCheckInById(todayCheckIn.id)
        }

        // 计算计数（这里可以根据需要调整逻辑）
        val previousCount = checkInDao.getCheckInCountByName(name)
        val newCount = previousCount + 1

        val checkIn = CheckIn(
            name = name,
            type = CheckInType.LOVE_DIARY,
            moodType = moodType,
            date = today,
            count = newCount
        )

        return checkInDao.insertCheckIn(checkIn)
    }

    // 打卡 - 普通习惯类型
    suspend fun checkInHabit(name: String, habitId: Long, tag: String? = null): Long {
        val config = getCheckInConfigByName(name)
        if (config == null) {
            // 如果配置不存在，创建一个默认配置
            val newConfig = CheckInConfig(
                name = name,
                type = CheckInType.HABIT,
                description = "打卡事项",
                buttonLabel = "打卡",
                icon = "✅"
            )
            saveCheckInConfig(newConfig)
        }

        val today = LocalDate.now().toString()
        val todayCheckIn = checkInDao.getCheckInByDateAndName(today, name)
        
        // 如果今天已经打卡，先删除旧记录
        if (todayCheckIn != null) {
            checkInDao.deleteCheckInById(todayCheckIn.id)
        }

        // 计算计数
        val previousCount = checkInDao.getCheckInCountByName(name)
        val newCount = previousCount + 1

        val checkIn = CheckIn(
            name = name,
            type = CheckInType.HABIT,
            habitId = habitId,
            tag = tag,
            date = today,
            count = newCount
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

    // 获取特定类型的打卡记录
    fun getCheckInsByType(type: CheckInType): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByType(type)
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
}