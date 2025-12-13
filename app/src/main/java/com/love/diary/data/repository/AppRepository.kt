package com.love.diary.data.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.DailyMoodDao
import com.love.diary.data.database.dao.EventDao
import com.love.diary.data.database.dao.HabitDao
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val database: LoveDatabase,
    private val eventDao: EventDao,
    private val checkInRepository: CheckInRepository
) {

    private val appConfigDao = database.appConfigDao()
    private val dailyMoodDao = database.dailyMoodDao()
    private val habitDao = database.habitDao()

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
    
    // === Habit Management ===
    
    // 获取所有活跃的打卡事项
    fun getAllHabits() = habitDao.getAllHabits()
    
    // 根据ID获取打卡事项
    suspend fun getHabitById(id: Long) = habitDao.getHabitById(id)
    
    // 创建新的打卡事项
    suspend fun createHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }
    
    // 更新打卡事项
    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }
    
    // 删除打卡事项（软删除）
    suspend fun deleteHabit(id: Long) {
        habitDao.deactivateHabit(id)
    }
    
    // 获取打卡记录
    fun getHabitRecordsFlow(habitId: Long) = habitDao.getHabitRecordsFlow(habitId)
    
    // 打卡
    suspend fun checkInHabit(habitId: Long, note: String? = null): Boolean {
        val habit = habitDao.getHabitById(habitId) ?: return false
        val today = LocalDate.now().toString()
        
        // 检查今天是否已经打卡
        val todayRecord = habitDao.getTodaysRecord(habitId, today)
        if (todayRecord != null) {
            return false // 今天已经打卡
        }
        
        // 计算新的计数
        val newCount = when (habit.type) {
            HabitType.POSITIVE -> {
                // 正向打卡：增加计数
                habit.currentCount + 1
            }
            HabitType.COUNTDOWN -> {
                // 倒计时：减少计数（如果目标日期已到达或过去）
                val targetDate = habit.targetDate?.let { LocalDate.parse(it) }
                val currentDate = LocalDate.now()
                if (targetDate != null) {
                    val daysUntilTarget = ChronoUnit.DAYS.between(currentDate, targetDate)
                    daysUntilTarget.toInt()
                } else {
                    habit.currentCount - 1
                }
            }
        }
        
        // 创建新的打卡记录
        val record = HabitRecord(
            habitId = habitId,
            count = newCount,
            note = note,
            date = today
        )
        
        val recordId = habitDao.insertHabitRecord(record)
        
        // 更新习惯的当前计数
        val updatedHabit = habit.copy(
            currentCount = newCount,
            isCompletedToday = true,
            updatedAt = System.currentTimeMillis()
        )
        habitDao.updateHabit(updatedHabit)
        
        return recordId > 0
    }
    
    // 重载打卡方法，支持标签
    suspend fun checkInHabitWithTag(habitId: Long, tag: String?): Boolean {
        return checkInHabit(habitId, tag)
    }
    
    // 获取打卡统计信息
    suspend fun getHabitStats(habitId: Long): Pair<Int, Int> {
        val totalRecords = habitDao.getHabitRecordCount(habitId)
        val latestRecord = habitDao.getLatestHabitRecord(habitId)
        val currentCount = latestRecord?.count ?: 0
        return Pair(currentCount, totalRecords)
    }

    // === 新增功能：通用事件管理 ===
    
    // 获取指定日期的所有事件
    suspend fun getEventsForDate(date: String): List<Event> {
        return eventDao.getEventsByDate(date)
    }

    // 获取特定类型的所有事件
    fun getEventsByType(eventType: EventType): Flow<List<Event>> {
        return eventDao.getEventsByType(eventType, Int.MAX_VALUE, 0)
    }

    // 获取指定时间段内的事件
    suspend fun getEventsBetweenDates(startDate: String, endDate: String): List<Event> {
        return eventDao.getEventsBetweenDates(startDate, endDate)
    }

    // 创建新事件
    suspend fun createEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    // 更新事件
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    // 删除事件
    suspend fun deleteEvent(eventId: Long) {
        eventDao.deleteEventById(eventId)
    }

    // 获取活动事件配置
    fun getActiveEventConfigs(): Flow<List<EventConfig>> {
        return eventDao.getAllActiveConfigs()
    }

    // 根据事件类型获取配置
    fun getEventConfigsByType(eventType: EventType): Flow<List<EventConfig>> {
        return eventDao.getConfigsByEventType(eventType)
    }

    // 获取事件配置详情
    suspend fun getEventConfigById(id: Long): EventConfig? {
        return eventDao.getConfigById(id)
    }

    // 创建事件配置
    suspend fun createEventConfig(config: EventConfig): Long {
        return eventDao.insertConfig(config)
    }

    // 更新事件配置
    suspend fun updateEventConfig(config: EventConfig) {
        eventDao.updateConfig(config)
    }

    // 删除事件配置
    suspend fun deleteEventConfig(id: Long) {
        eventDao.deactivateConfig(id) // 软删除
    }

    // === 新增功能：统一打卡管理 ===
    
    // 获取所有打卡配置
    fun getAllCheckInConfigs(): Flow<List<UnifiedCheckInConfig>> {
        return checkInRepository.getAllCheckInConfigs()
    }

    // 根据类型获取打卡配置
    fun getCheckInConfigsByType(type: CheckInType): Flow<List<UnifiedCheckInConfig>> {
        return checkInRepository.getCheckInConfigsByType(type)
    }

    // 获取特定打卡事项的记录
    fun getCheckInsByName(name: String): Flow<List<UnifiedCheckIn>> {
        return checkInRepository.getCheckInsByName(name)
    }

    // 获取特定日期的打卡记录
    fun getCheckInsByDate(date: String): Flow<List<UnifiedCheckIn>> {
        return checkInRepository.getCheckInsByDate(date)
    }

    // 获取指定日期范围内的打卡记录
    fun getCheckInsBetweenDates(startDate: String, endDate: String): Flow<List<UnifiedCheckIn>> {
        return checkInRepository.getCheckInsBetweenDates(startDate, endDate)
    }

    // 获取特定类型的打卡记录
    fun getCheckInsByType(type: CheckInType): Flow<List<UnifiedCheckIn>> {
        return checkInRepository.getCheckInsByType(type)
    }

    // 获取特定类型和日期范围内的打卡记录
    fun getCheckInsByTypeAndDateRange(type: CheckInType, startDate: String, endDate: String): Flow<List<UnifiedCheckIn>> {
        return checkInRepository.getCheckInsByTypeAndDateRange(type, startDate, endDate)
    }

    // 获取所有唯一的打卡类型
    fun getUniqueCheckInTypes(): Flow<List<CheckInType>> {
        return checkInRepository.getUniqueCheckInTypes()
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
        return checkInRepository.checkIn(
            name = name,
            type = type,
            moodType = moodType,
            tag = tag,
            note = note,
            attachmentUri = attachmentUri,
            duration = duration,
            rating = rating,
            count = count
        )
    }

    // 恋爱时间记录打卡（特殊打卡类型）
    suspend fun checkInLoveDiary(
        name: String = "恋爱日记",
        moodType: MoodType,
        note: String? = null,
        attachmentUri: String? = null
    ): Long {
        return checkInRepository.checkInLoveDiary(
            name = name,
            moodType = moodType,
            note = note,
            attachmentUri = attachmentUri
        )
    }

    // 普通打卡事项
    suspend fun checkInHabit(
        name: String,
        tag: String? = null,
        note: String? = null,
        attachmentUri: String? = null
    ): Long {
        return checkInRepository.checkInHabit(
            name = name,
            tag = tag,
            note = note,
            attachmentUri = attachmentUri
        )
    }

    // 里程碑事件打卡
    suspend fun checkInMilestone(
        name: String,
        note: String? = null,
        attachmentUri: String? = null,
        rating: Int? = null
    ): Long {
        return checkInRepository.checkInMilestone(
            name = name,
            note = note,
            attachmentUri = attachmentUri,
            rating = rating
        )
    }

    // 日常任务打卡
    suspend fun checkInDailyTask(
        name: String,
        note: String? = null,
        duration: Int? = null,
        isCompleted: Boolean = true
    ): Long {
        return checkInRepository.checkInDailyTask(
            name = name,
            note = note,
            duration = duration,
            isCompleted = isCompleted
        )
    }

    // 获取恋爱日记记录
    fun getLoveDiaryRecords(): Flow<List<UnifiedCheckIn>> {
        return checkInRepository.getLoveDiaryRecords()
    }

    // 获取指定日期范围内的恋爱日记记录
    suspend fun getLoveDiaryRecordsBetweenDates(startDate: String, endDate: String): List<UnifiedCheckIn> {
        return checkInRepository.getLoveDiaryRecordsBetweenDates(startDate, endDate)
    }

    // 获取最新的恋爱日记记录
    suspend fun getLatestLoveDiaryRecord(): UnifiedCheckIn? {
        return checkInRepository.getLatestLoveDiaryRecord()
    }

    // 获取打卡趋势
    suspend fun getCheckInTrendByName(name: String): List<CheckInTrend> {
        return checkInRepository.getCheckInTrendByName(name)
    }

    // 获取指定数量的最近打卡记录
    suspend fun getRecentCheckInsByName(name: String, limit: Int): List<UnifiedCheckIn> {
        return checkInRepository.getRecentCheckInsByName(name, limit)
    }

    // 批量插入打卡记录
    suspend fun insertCheckIns(checkIns: List<UnifiedCheckIn>): List<Long> {
        return checkInRepository.insertCheckIns(checkIns)
    }

    // 更新打卡记录
    suspend fun updateCheckIn(checkIn: UnifiedCheckIn) {
        checkInRepository.updateCheckIn(checkIn)
    }

    // 删除打卡记录
    suspend fun deleteCheckIn(id: Long) {
        checkInRepository.deleteCheckIn(id)
    }
}
