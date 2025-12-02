package com.love.diary.data.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.DailyMoodDao
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val database: LoveDatabase
) {

    private val appConfigDao = database.appConfigDao()
    private val dailyMoodDao = database.dailyMoodDao()

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    // 在 AppRepository 类中添加
    suspend fun initializeFirstRun(
        startDate: String,
        coupleName: String? = null,
        partnerNickname: String? = null
    ) {
        val config = AppConfigEntity(
            startDate = startDate,
            startTimeMinutes = 0,
            coupleName = coupleName,
            partnerNickname = partnerNickname,
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveAppConfig(config)
    }

    suspend fun isFirstRun(): Boolean {
        return appConfigDao.getConfig() == null
    }

    // === App Config ===
    suspend fun getAppConfig(): AppConfigEntity? {
        return appConfigDao.getConfig()
    }

    fun getAppConfigFlow(): Flow<AppConfigEntity?> {
        return appConfigDao.getConfigFlow()
    }

    suspend fun saveAppConfig(config: AppConfigEntity) {
        appConfigDao.insertConfig(config)
    }

    suspend fun updateAppConfig(config: AppConfigEntity) {
        appConfigDao.updateConfig(config)
    }
    
    suspend fun deleteAppConfig() {
        appConfigDao.deleteConfig()
    }

    // === Daily Mood ===
    suspend fun saveTodayMood(
        moodType: MoodType,
        moodText: String? = null
    ): Long {
        val today = getTodayDateString()

        val config = getAppConfig()
        val dayIndex = config?.let {
            calculateDayIndex(it.startDate, today)
        } ?: 1

        val anniversaryInfo = checkAnniversary(dayIndex)

        val moodEntity = DailyMoodEntity(
            date = today,
            dayIndex = dayIndex,
            moodTypeCode = moodType.code,
            moodScore = moodType.score,
            moodText = moodText,
            hasText = moodText?.isNotBlank() ?: false,
            isAnniversary = anniversaryInfo.isAnniversary,
            anniversaryType = anniversaryInfo.type,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        return dailyMoodDao.insertMood(moodEntity)
    }

    suspend fun getTodayMood(): DailyMoodEntity? {
        val today = getTodayDateString()
        return dailyMoodDao.getMoodByDate(today)
    }

    fun getRecentMoods(limit: Int = 50): Flow<List<DailyMoodEntity>> {
        return dailyMoodDao.getRecentMoods(limit, 0)
    }

    suspend fun getMoodsBetweenDates(startDate: String, endDate: String): List<DailyMoodEntity> {
        return dailyMoodDao.getMoodsBetweenDates(startDate, endDate)
    }

    // 获取心情趋势数据
    suspend fun getMoodTrendBetweenDates(startDate: String, endDate: String): List<DailyMoodDao.DailyMoodScore> {
        return dailyMoodDao.getMoodTrendBetweenDates(startDate, endDate)
    }

    // === 新增方法：获取所有心情记录 ===
    suspend fun getAllMoodRecords(): List<DailyMoodEntity> {
        return dailyMoodDao.getMoodsBetweenDates(
            startDate = "1900-01-01", // 足够早的日期
            endDate = LocalDate.now().plusYears(100).toString() // 足够晚的日期
        )
    }

    // === 新增方法：清除所有心情记录（用于数据恢复） ===
    suspend fun clearAllMoodRecords() {
        dailyMoodDao.deleteAllMoods()
    }

    // === 新增方法：批量插入心情记录（用于数据恢复） ===
    suspend fun batchInsertMoodRecords(records: List<DailyMoodEntity>) {
        dailyMoodDao.insertMoods(records)
    }

    // === Helper Methods ===
    private fun getTodayDateString(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    private fun calculateDayIndex(startDate: String, targetDate: String): Int {
        val start = LocalDate.parse(startDate)
        val target = LocalDate.parse(targetDate)
        return start.until(target).days + 1
    }

    data class AnniversaryInfo(
        val isAnniversary: Boolean = false,
        val type: String? = null
    )

    private fun checkAnniversary(dayIndex: Int): AnniversaryInfo {
        if (dayIndex % 100 == 0) {
            return AnniversaryInfo(
                isAnniversary = true,
                type = "DAY_$dayIndex"
            )
        }
        return AnniversaryInfo(false)
    }

    fun getDayDisplay(dayIndex: Int): String {
        val years = dayIndex / 365
        val months = (dayIndex % 365) / 30
        val days = (dayIndex % 365) % 30

        return buildString {
            if (years > 0) append("${years}年")
            if (months > 0) append("${months}个月")
            if (days > 0 || (years == 0 && months == 0)) append("${days}天")
        }
    }
}
