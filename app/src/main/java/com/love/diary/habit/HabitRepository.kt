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
}