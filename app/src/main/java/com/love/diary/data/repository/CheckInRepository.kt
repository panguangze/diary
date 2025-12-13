package com.love.diary.data.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.model.UnifiedCheckInConfig
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.CheckInTrend
import com.love.diary.data.model.MoodType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class CheckInRepository @Inject constructor(
    private val database: LoveDatabase
) {
    private val unifiedCheckInDao = database.unifiedCheckInDao()

    // 获取所有打卡配置
    fun getAllCheckInConfigs(): Flow<List<UnifiedCheckInConfig>> {
        return unifiedCheckInDao.getAllCheckInConfigs()
    }

    // 根据类型获取打卡配置
    fun getCheckInConfigsByType(type: CheckInType): Flow<List<UnifiedCheckInConfig>> {
        return unifiedCheckInDao.getCheckInConfigsByType(type)
    }

    // 根据ID获取打卡配置
    suspend fun getCheckInConfigById(id: Long): UnifiedCheckInConfig? {
        return unifiedCheckInDao.getCheckInConfigById(id)
    }

    // 根据名称获取打卡配置
    suspend fun getCheckInConfigByName(name: String): UnifiedCheckInConfig? {
        return unifiedCheckInDao.getCheckInConfigByName(name)
    }

    // 创建或更新打卡配置
    suspend fun saveCheckInConfig(config: UnifiedCheckInConfig): Long {
        return unifiedCheckInDao.insertCheckInConfig(config)
    }

    // 更新打卡配置
    suspend fun updateCheckInConfig(config: UnifiedCheckInConfig) {
        unifiedCheckInDao.updateCheckInConfig(config)
    }

    // 删除打卡配置（软删除）
    suspend fun deleteCheckInConfig(id: Long) {
        unifiedCheckInDao.deactivateCheckInConfig(id)
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
        count: Int = 1,
        configId: Long? = null
    ): Long {
        val config = getCheckInConfigByName(name)
        if (config == null) {
            // 如果配置不存在，创建一个默认配置
            val newConfig = UnifiedCheckInConfig(
                name = name,
                type = type,
                description = when(type) {
                    CheckInType.LOVE_DIARY -> "恋爱时间记录"
                    CheckInType.HABIT -> "习惯养成打卡"
                    CheckInType.EXERCISE -> "运动打卡"
                    CheckInType.STUDY -> "学习打卡"
                    CheckInType.WORKOUT -> "健身打卡"
                    CheckInType.DIET -> "饮食打卡"
                    CheckInType.MEDITATION -> "冥想打卡"
                    CheckInType.READING -> "阅读打卡"
                    CheckInType.WATER -> "喝水打卡"
                    CheckInType.SLEEP -> "睡眠打卡"
                    CheckInType.MILESTONE -> "里程碑事件"
                    CheckInType.CUSTOM -> "自定义打卡"
                },
                buttonLabel = when(type) {
                    CheckInType.LOVE_DIARY -> "记录恋爱时光"
                    CheckInType.HABIT -> "打卡"
                    CheckInType.EXERCISE -> "运动"
                    CheckInType.STUDY -> "学习"
                    CheckInType.WORKOUT -> "健身"
                    CheckInType.DIET -> "饮食"
                    CheckInType.MEDITATION -> "冥想"
                    CheckInType.READING -> "阅读"
                    CheckInType.WATER -> "喝水"
                    CheckInType.SLEEP -> "睡眠"
                    CheckInType.MILESTONE -> "里程碑"
                    CheckInType.CUSTOM -> "打卡"
                },
                icon = when(type) {
                    CheckInType.LOVE_DIARY -> "❤️"
                    CheckInType.HABIT -> "✅"
                    CheckInType.EXERCISE -> "🏃"
                    CheckInType.STUDY -> "📚"
                    CheckInType.WORKOUT -> "💪"
                    CheckInType.DIET -> "🥗"
                    CheckInType.MEDITATION -> "🧘"
                    CheckInType.READING -> "📖"
                    CheckInType.WATER -> "💧"
                    CheckInType.SLEEP -> "😴"
                    CheckInType.MILESTONE -> "🏆"
                    CheckInType.CUSTOM -> "🎯"
                }
            )
            saveCheckInConfig(newConfig)
        }

        val today = LocalDate.now().toString()
        val todayCheckIn = unifiedCheckInDao.getCheckInByDateAndName(today, name)
        
        // 如果今天已经打卡，先删除旧记录
        if (todayCheckIn != null) {
            unifiedCheckInDao.deleteCheckInById(todayCheckIn.id)
        }

        val unifiedCheckIn = UnifiedCheckIn(
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
            isCompleted = true,
            configId = configId
        )

        return unifiedCheckInDao.insertCheckIn(unifiedCheckIn)
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

    // 打卡 - 运动类型
    suspend fun checkInExercise(
        name: String,
        note: String? = null,
        duration: Int? = null,
        rating: Int? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.EXERCISE,
            note = note,
            duration = duration,
            rating = rating
        )
    }

    // 打卡 - 学习类型
    suspend fun checkInStudy(
        name: String,
        note: String? = null,
        duration: Int? = null,
        count: Int = 1
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.STUDY,
            note = note,
            duration = duration,
            count = count
        )
    }

    // 打卡 - 健身类型
    suspend fun checkInWorkout(
        name: String,
        note: String? = null,
        duration: Int? = null,
        rating: Int? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.WORKOUT,
            note = note,
            duration = duration,
            rating = rating
        )
    }

    // 打卡 - 饮食类型
    suspend fun checkInDiet(
        name: String,
        note: String? = null,
        tag: String? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.DIET,
            note = note,
            tag = tag
        )
    }

    // 打卡 - 冥想类型
    suspend fun checkInMeditation(
        name: String,
        note: String? = null,
        duration: Int? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.MEDITATION,
            note = note,
            duration = duration
        )
    }

    // 打卡 - 阅读类型
    suspend fun checkInReading(
        name: String,
        note: String? = null,
        duration: Int? = null,
        count: Int = 1
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.READING,
            note = note,
            duration = duration,
            count = count
        )
    }

    // 打卡 - 喝水类型
    suspend fun checkInWater(
        name: String,
        count: Int = 1,
        note: String? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.WATER,
            count = count,
            note = note
        )
    }

    // 打卡 - 睡眠类型
    suspend fun checkInSleep(
        name: String,
        duration: Int? = null,
        moodType: MoodType? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.SLEEP,
            duration = duration,
            moodType = moodType
        )
    }

    // 打卡 - 自定义类型
    suspend fun checkInCustom(
        name: String,
        type: CheckInType = CheckInType.CUSTOM,
        note: String? = null,
        tag: String? = null,
        count: Int = 1
    ): Long {
        return checkIn(
            name = name,
            type = type,
            note = note,
            tag = tag,
            count = count
        )
    }

    // 打卡 - 里程碑事件类型
    suspend fun checkInMilestone(
        name: String,
        note: String? = null,
        attachmentUri: String? = null,
        rating: Int? = null
    ): Long {
        return checkIn(
            name = name,
            type = CheckInType.MILESTONE, // Using dedicated MILESTONE type
            note = note,
            attachmentUri = attachmentUri,
            rating = rating
        )
    }

    // 获取特定打卡事项的记录
    fun getCheckInsByName(name: String): Flow<List<UnifiedCheckIn>> {
        return unifiedCheckInDao.getCheckInsByName(name)
    }

    // 获取特定日期的打卡记录
    fun getCheckInsByDate(date: String): Flow<List<UnifiedCheckIn>> {
        return unifiedCheckInDao.getCheckInsByDate(date)
    }

    // 获取指定日期范围内的打卡记录
    fun getCheckInsBetweenDates(startDate: String, endDate: String): Flow<List<UnifiedCheckIn>> {
        return unifiedCheckInDao.getCheckInsBetweenDates(startDate, endDate)
    }

    // 获取特定类型的打卡记录
    fun getCheckInsByType(type: CheckInType): Flow<List<UnifiedCheckIn>> {
        return unifiedCheckInDao.getCheckInsByType(type)
    }

    // 获取特定类型和日期范围内的打卡记录
    fun getCheckInsByTypeAndDateRange(type: CheckInType, startDate: String, endDate: String): Flow<List<UnifiedCheckIn>> {
        return unifiedCheckInDao.getCheckInsByTypeAndDateRange(type, startDate, endDate)
    }

    // 获取所有唯一的打卡类型
    fun getUniqueCheckInTypes(): Flow<List<CheckInType>> {
        return unifiedCheckInDao.getUniqueCheckInTypes()
    }

    // 获取打卡统计
    suspend fun getCheckInCountByName(name: String): Int {
        return unifiedCheckInDao.getCheckInCountByName(name)
    }

    // 获取最近的打卡记录
    suspend fun getRecentCheckInsByName(name: String, limit: Int): List<UnifiedCheckIn> {
        return unifiedCheckInDao.getRecentCheckInsByName(name, limit)
    }

    // 获取打卡趋势
    suspend fun getCheckInTrendByName(name: String): List<CheckInTrend> {
        return unifiedCheckInDao.getCheckInTrendByName(name)
    }

    // 获取恋爱日记记录
    fun getLoveDiaryRecords(): Flow<List<UnifiedCheckIn>> {
        return unifiedCheckInDao.getLoveDiaryRecords()
    }

    // 获取指定日期范围内的恋爱日记记录
    suspend fun getLoveDiaryRecordsBetweenDates(startDate: String, endDate: String): List<UnifiedCheckIn> {
        return unifiedCheckInDao.getLoveDiaryRecordsBetweenDates(startDate, endDate)
    }

    // 获取最新的恋爱日记记录
    suspend fun getLatestLoveDiaryRecord(): UnifiedCheckIn? {
        return unifiedCheckInDao.getLatestLoveDiaryRecord()
    }

    // 批量插入打卡记录
    suspend fun insertCheckIns(checkIns: List<UnifiedCheckIn>): List<Long> {
        return unifiedCheckInDao.insertCheckIns(checkIns)
    }

    // 更新打卡记录
    suspend fun updateCheckIn(checkIn: UnifiedCheckIn) {
        unifiedCheckInDao.updateCheckIn(checkIn)
    }

    // 删除打卡记录
    suspend fun deleteCheckIn(id: Long) {
        unifiedCheckInDao.deleteCheckInById(id)
    }
}