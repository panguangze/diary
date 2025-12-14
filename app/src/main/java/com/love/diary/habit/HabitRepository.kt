package com.love.diary.habit

import android.content.Context
import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import com.love.diary.data.model.HabitType
import com.love.diary.data.model.UnifiedCheckIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

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
                metadata = """{"legacyHabitId":$habitId,"tags":"${habit.tags}"}"""
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
            val updatedConfig = it.copy(
                description = habit.description,
                buttonLabel = habit.buttonLabel,
                targetDate = habit.targetDate,
                icon = habit.icon,
                color = habit.color,
                isActive = habit.isActive,
                metadata = """{"legacyHabitId":${habit.id},"tags":"${habit.tags}"}""",
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
            
            // DUAL WRITE: Write to BOTH systems
            
            // 1. Write to UnifiedCheckIn (new unified system)
            val checkInId = database.unifiedCheckInDao().insertCheckIn(
                UnifiedCheckIn(
                    name = it.name,
                    type = CheckInType.HABIT,
                    tag = tag,
                    date = today,
                    count = newCount,
                    note = tag,
                    isCompleted = true,
                    metadata = """{"habitId":$habitId,"habitType":"${it.type}"}"""
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
            
            // 3. Update habit state
            val updatedHabit = it.copy(
                currentCount = newCount,
                isCompletedToday = true,
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