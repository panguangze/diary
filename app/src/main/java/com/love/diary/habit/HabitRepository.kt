package com.love.diary.habit

import android.content.Context
import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.model.Habit
import kotlinx.coroutines.flow.Flow

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

class DefaultHabitRepository(private val database: LoveDatabase) : HabitRepository {
    override fun getAllHabits(): Flow<List<Habit>> {
        return database.habitDao().getAllHabits()
    }
    
    override suspend fun getHabitById(id: Long): Habit? {
        return database.habitDao().getHabitById(id)
    }
    
    override suspend fun insertHabit(habit: Habit): Long {
        return database.habitDao().insertHabit(habit)
    }
    
    override suspend fun updateHabit(habit: Habit) {
        database.habitDao().updateHabit(habit)
    }
    
    override suspend fun deleteHabit(id: Long) {
        // 使用deactivate而不是删除，以保留数据
        database.habitDao().deactivateHabit(id)
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
    
    override suspend fun checkInHabit(habitId: Long, tag: String?): Boolean {
        val habit = database.habitDao().getHabitById(habitId)
        habit?.let {
            // 检查今天是否已经打卡
            val today = LocalDate.now().toString()
            val todayRecord = database.habitDao().getTodaysRecord(habitId, today)
            if (todayRecord != null) {
                return false // 今天已经打卡
            }
            
            // 计算新的计数
            val newCount = when (it.type) {
                HabitType.POSITIVE -> {
                    // 正向打卡：增加计数
                    it.currentCount + 1
                }
                HabitType.COUNTDOWN -> {
                    // 倒计时：减少计数（如果目标日期已到达或过去）
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
            
            // 创建新的打卡记录
            val record = HabitRecord(
                habitId = habitId,
                count = newCount,
                note = tag, // 使用标签作为备注
                date = today
            )
            
            val recordId = database.habitDao().insertHabitRecord(record)
            
            // 更新习惯的当前计数
            val updatedHabit = it.copy(
                currentCount = newCount,
                isCompletedToday = true,
                updatedAt = System.currentTimeMillis()
            )
            database.habitDao().updateHabit(updatedHabit)
            
            return recordId > 0
        }
        return false
    }
}