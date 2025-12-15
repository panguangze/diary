package com.love.diary.data.database.dao

import androidx.room.*
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    // Habit 相关操作
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE name = :name AND isActive = 1")
    suspend fun getHabitByName(name: String): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET isActive = 0 WHERE id = :id")
    suspend fun deactivateHabit(id: Long)

    // HabitRecord 相关操作
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getHabitRecords(habitId: Long, limit: Int, offset: Int): List<HabitRecord>

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY createdAt DESC")
    fun getHabitRecordsFlow(habitId: Long): Flow<List<HabitRecord>>

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun getHabitRecordByDate(habitId: Long, date: String): HabitRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitRecord(record: HabitRecord): Long

    @Query("DELETE FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun deleteHabitRecordByDate(habitId: Long, date: String)

    // 获取今日打卡记录
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun getTodaysRecord(habitId: Long, date: String): HabitRecord?

    // 获取习惯的总打卡次数
    @Query("SELECT COUNT(*) FROM habit_records WHERE habitId = :habitId")
    suspend fun getHabitRecordCount(habitId: Long): Int

    // 获取最近的打卡记录
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestHabitRecord(habitId: Long): HabitRecord?
}