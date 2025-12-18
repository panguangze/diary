package com.love.diary.habit

import android.content.Context
import com.google.gson.Gson
import com.love.diary.data.database.Converters
import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import com.love.diary.data.model.HabitType
import com.love.diary.data.model.UnifiedCheckIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Data classes for JSON metadata serialization.
 * 
 * These strongly-typed data classes are used with Gson for safe JSON serialization.
 * Security Note: Using data classes with Gson is safe because:
 * 1. Input data comes from Room database entities (trusted source)
 * 2. Data classes ensure type safety at compile time
 * 3. Gson properly escapes special characters during serialization
 * 4. No user input is directly serialized without validation
 * 5. The metadata is stored in the database, not exposed to external APIs
 */
private data class HabitMetadata(
    val legacyHabitId: Long,
    val tags: String
)

private data class CheckInMetadata(
    val habitId: Long,
    val habitType: String
)

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Long): Habit?
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: Long)
    suspend fun toggleHabit(habitId: Long)
    suspend fun checkInHabit(habitId: Long, tag: String? = null): Boolean
    
    companion object {
        @Volatile
        private var INSTANCE: HabitRepository? = null
        
        fun getInstance(context: Context): HabitRepository {
            return INSTANCE ?: synchronized(this) {
                val newInstance = DefaultHabitRepository(LoveDatabase.getInstance(context.applicationContext))
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}

/**
 * Default implementation of HabitRepository with bridge to UnifiedCheckIn system.
 * 
 * This repository maintains backward compatibility with the legacy Habit system
 * while transitioning to the UnifiedCheckIn system. During the transition:
 * - Writes go to BOTH systems (dual-write for data consistency)
 * - Reads come from UnifiedCheckIn when possible
 * - Legacy Habit tables maintained for backward compatibility
 * 
 * Migration Strategy:
 * 1. Current: Dual-write to both systems
 * 2. Future: Read only from UnifiedCheckIn
 * 3. Eventually: Deprecate legacy Habit tables
 */
class DefaultHabitRepository(private val database: LoveDatabase) : HabitRepository {
    private val gson = Gson()
    
    override fun getAllHabits(): Flow<List<Habit>> {
        return database.habitDao().getAllHabits()
    }
    
    override suspend fun getHabitById(id: Long): Habit? {
        return database.habitDao().getHabitById(id)
    }
    
    override suspend fun insertHabit(habit: Habit): Long {
        // Write to legacy Habit table
        val habitId = database.habitDao().insertHabit(habit)
        
        // Also create a UnifiedCheckInConfig for this habit
        // This enables the habit to work with the unified system
        val existingConfig = database.unifiedCheckInDao().getCheckInConfigByName(habit.name)
        if (existingConfig == null) {
            val metadata = HabitMetadata(
                legacyHabitId = habitId,
                tags = habit.tags
            )
            val config = com.love.diary.data.model.UnifiedCheckInConfig(
                name = habit.name,
                type = CheckInType.HABIT,
                description = habit.description,
                buttonLabel = habit.buttonLabel,
                startDate = habit.startDate,
                targetDate = habit.targetDate,
                icon = habit.icon,
                color = habit.color,
                isActive = habit.isActive,
                metadata = gson.toJson(metadata)
            )
            database.unifiedCheckInDao().insertCheckInConfig(config)
        }
        
        return habitId
    }
    
    override suspend fun updateHabit(habit: Habit) {
        // Update legacy Habit table
        database.habitDao().updateHabit(habit)
        
        // Also update the corresponding UnifiedCheckInConfig
        val config = database.unifiedCheckInDao().getCheckInConfigByName(habit.name)
        config?.let {
            val metadata = HabitMetadata(
                legacyHabitId = habit.id,
                tags = habit.tags
            )
            val updatedConfig = it.copy(
                description = habit.description,
                buttonLabel = habit.buttonLabel,
                targetDate = habit.targetDate,
                icon = habit.icon,
                color = habit.color,
                isActive = habit.isActive,
                metadata = gson.toJson(metadata),
                updatedAt = System.currentTimeMillis()
            )
            database.unifiedCheckInDao().updateCheckInConfig(updatedConfig)
        }
    }
    
    override suspend fun deleteHabit(id: Long) {
        // Soft delete in legacy system
        database.habitDao().deactivateHabit(id)
        
        // Also deactivate the corresponding UnifiedCheckInConfig
        val habit = database.habitDao().getHabitById(id)
        habit?.let {
            val config = database.unifiedCheckInDao().getCheckInConfigByName(it.name)
            config?.let { cfg ->
                database.unifiedCheckInDao().deactivateCheckInConfig(cfg.id)
            }
        }
    }
    
    override suspend fun toggleHabit(habitId: Long) {
        val habit = database.habitDao().getHabitById(habitId)
        habit?.let {
            val updatedHabit = it.copy(
                currentCount = it.currentCount + 1,
                isCompletedToday = true,
                updatedAt = System.currentTimeMillis()
            )
            database.habitDao().updateHabit(updatedHabit)
        }
    }
    
    /**
     * Check in a habit - writes to BOTH legacy and unified systems.
     * 
     * This dual-write approach ensures:
     * 1. Backward compatibility with legacy system
     * 2. Data available in unified system for new features
     * 3. Gradual migration path
     * 
     * @param habitId The ID of the habit to check in
     * @param tag Optional tag/note for the check-in
     * @return true if check-in successful, false otherwise
     */
    override suspend fun checkInHabit(habitId: Long, tag: String?): Boolean {
        val habit = database.habitDao().getHabitById(habitId)
        habit?.let {
            // Check if already checked in today
            val today = LocalDate.now().toString()
            val todayRecord = database.habitDao().getTodaysRecord(habitId, today)
            if (todayRecord != null) {
                return false // Already checked in today
            }
            
            // Calculate new count
            val newCount = when (it.type) {
                HabitType.POSITIVE -> {
                    it.currentCount + 1
                }
                HabitType.COUNTDOWN -> {
                    val targetDate = it.targetDate?.let { LocalDate.parse(it) }
                    val currentDate = LocalDate.now()
                    if (targetDate != null) {
                        val daysUntilTarget = java.time.temporal.ChronoUnit.DAYS.between(currentDate, targetDate)
                        daysUntilTarget.toInt()
                    } else {
                        it.currentCount - 1
                    }
                }
            }
            
            // 计算打卡统计数据
            val todayDate = LocalDate.now()
            val yesterdayDate = todayDate.minusDays(1)
            val yesterdayString = yesterdayDate.toString()
            
            // 检查昨天是否打卡
            val yesterdayRecord = database.habitDao().getTodaysRecord(habitId, yesterdayString)
            val newCurrentStreak = if (yesterdayRecord != null || it.currentStreak == 0) {
                it.currentStreak + 1
            } else {
                1 // 重置连续天数
            }
            
            val newLongestStreak = if (newCurrentStreak > it.longestStreak) newCurrentStreak else it.longestStreak
            val newTotalCheckIns = it.totalCheckIns + 1
            
            // DUAL WRITE: Write to BOTH systems
            
            // 1. Write to UnifiedCheckIn (new unified system)
            val checkInMetadata = CheckInMetadata(
                habitId = habitId,
                habitType = it.type.toString()
            )
            val checkInId = database.unifiedCheckInDao().insertCheckIn(
                UnifiedCheckIn(
                    name = it.name,
                    type = CheckInType.HABIT,
                    tag = tag,
                    date = today,
                    count = newCount,
                    note = tag,
                    isCompleted = true,
                    metadata = gson.toJson(checkInMetadata)
                )
            )
            
            // 2. Write to legacy HabitRecord (for backward compatibility)
            val record = HabitRecord(
                habitId = habitId,
                count = newCount,
                note = tag,
                date = today
            )
            val recordId = database.habitDao().insertHabitRecord(record)
            
            // 3. Update habit state with new statistics
            val updatedHabit = it.copy(
                currentCount = newCount,
                isCompletedToday = true,
                currentStreak = newCurrentStreak,
                longestStreak = newLongestStreak,
                totalCheckIns = newTotalCheckIns,
                updatedAt = System.currentTimeMillis()
            )
            database.habitDao().updateHabit(updatedHabit)
            
            return checkInId > 0 && recordId > 0
        }
        return false
    }
    
    /**
     * Get check-in history for a habit from the UnifiedCheckIn system.
     * 
     * This demonstrates reading from the unified system while maintaining
     * compatibility with the legacy Habit system.
     * 
     * @param habitId The ID of the habit
     * @return List of check-ins from the unified system
     */
    suspend fun getCheckInHistoryFromUnified(habitId: Long): List<UnifiedCheckIn> {
        val habit = database.habitDao().getHabitById(habitId) ?: return emptyList()
        return database.unifiedCheckInDao()
            .getCheckInsByName(habit.name)
            .first()
    }
    
    /**
     * Get check-in history for a habit from the legacy system.
     * 
     * Maintained for comparison and backward compatibility during transition.
     * 
     * @param habitId The ID of the habit
     * @return List of habit records from the legacy system
     */
    suspend fun getCheckInHistoryFromLegacy(habitId: Long): List<HabitRecord> {
        return database.habitDao().getHabitRecordsFlow(habitId).first()
    }
}