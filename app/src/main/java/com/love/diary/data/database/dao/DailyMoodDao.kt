package com.love.diary.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.love.diary.data.database.entities.DailyMoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: DailyMoodEntity): Long

    @Update
    suspend fun updateMood(mood: DailyMoodEntity)

    @Query("SELECT * FROM daily_mood WHERE date = :date AND deleted = 0")
    suspend fun getMoodByDate(date: String): DailyMoodEntity?

    @Query("SELECT * FROM daily_mood WHERE deleted = 0 ORDER BY date DESC LIMIT :limit OFFSET :offset")
    fun getRecentMoods(limit: Int, offset: Int): Flow<List<DailyMoodEntity>>

    @Query("DELETE FROM daily_mood WHERE deleted = 0")
    suspend fun deleteAllMoods()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<DailyMoodEntity>)

    @Query("""
        SELECT * FROM daily_mood 
        WHERE deleted = 0 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    suspend fun getMoodsBetweenDates(
        startDate: String,
        endDate: String
    ): List<DailyMoodEntity>

    @Query("""
        SELECT COUNT(*) FROM daily_mood 
        WHERE deleted = 0 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getRecordCountBetweenDates(
        startDate: String,
        endDate: String
    ): Int

    // ✅ 用 data class 承接统计结果
    data class MoodStats(
        val moodTypeCode: String,
        val count: Int
    )

    @Query("""
        SELECT moodTypeCode, COUNT(*) as count 
        FROM daily_mood 
        WHERE deleted = 0 
        AND date BETWEEN :startDate AND :endDate 
        GROUP BY moodTypeCode
    """)
    suspend fun getMoodStatsBetweenDates(
        startDate: String,
        endDate: String
    ): List<MoodStats>

    @Query("""
        SELECT date, moodScore 
        FROM daily_mood 
        WHERE deleted = 0 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date ASC
    """)
    suspend fun getMoodTrendBetweenDates(
        startDate: String,
        endDate: String
    ): List<DailyMoodScore>

    data class DailyMoodScore(
        val date: String,
        val moodScore: Int
    )
}