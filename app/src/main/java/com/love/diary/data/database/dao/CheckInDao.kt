package com.love.diary.data.database.dao

import androidx.room.*
import com.love.diary.data.model.CheckIn
import com.love.diary.data.model.CheckInConfig
import com.love.diary.data.model.CheckInTrend
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    // CheckIn 相关操作
    @Query("SELECT * FROM checkins WHERE type = :type ORDER BY createdAt DESC")
    fun getCheckInsByType(type: com.love.diary.data.model.CheckInType): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE date = :date ORDER BY createdAt DESC")
    fun getCheckInsByDate(date: String): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE name = :name ORDER BY createdAt DESC")
    fun getCheckInsByName(name: String): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getCheckInsBetweenDates(startDate: String, endDate: String): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE type = :type AND date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getCheckInsByTypeAndDateRange(type: com.love.diary.data.model.CheckInType, startDate: String, endDate: String): Flow<List<CheckIn>>

    @Query("SELECT DISTINCT type FROM checkins")
    fun getUniqueCheckInTypes(): Flow<List<com.love.diary.data.model.CheckInType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckIn): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIns(checkIns: List<CheckIn>): List<Long>

    @Update
    suspend fun updateCheckIn(checkIn: CheckIn)

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckIn)

    @Query("DELETE FROM checkins WHERE id = :id")
    suspend fun deleteCheckInById(id: Long)

    // CheckInConfig 相关操作
    @Query("SELECT * FROM checkin_configs WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllCheckInConfigs(): Flow<List<CheckInConfig>>

    @Query("SELECT * FROM checkin_configs WHERE type = :type AND isActive = 1 ORDER BY updatedAt DESC")
    fun getCheckInConfigsByType(type: com.love.diary.data.model.CheckInType): Flow<List<CheckInConfig>>

    @Query("SELECT * FROM checkin_configs WHERE id = :id")
    suspend fun getCheckInConfigById(id: Long): CheckInConfig?

    @Query("SELECT * FROM checkin_configs WHERE name = :name AND isActive = 1")
    suspend fun getCheckInConfigByName(name: String): CheckInConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckInConfig(config: CheckInConfig): Long

    @Update
    suspend fun updateCheckInConfig(config: CheckInConfig)

    @Query("UPDATE checkin_configs SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCheckInConfig(id: Long)

    // 获取特定日期的打卡记录
    @Query("SELECT * FROM checkins WHERE date = :date AND name = :name")
    suspend fun getCheckInByDateAndName(date: String, name: String): CheckIn?

    // 获取某个打卡事项的统计信息
    @Query("SELECT COUNT(*) FROM checkins WHERE name = :name")
    suspend fun getCheckInCountByName(name: String): Int

    // 获取某个打卡事项的最近记录
    @Query("SELECT * FROM checkins WHERE name = :name ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentCheckInsByName(name: String, limit: Int): List<CheckIn>

    // 获取某个打卡事项的记录趋势
    @Query("SELECT date, COUNT(*) as count FROM checkins WHERE name = :name GROUP BY date ORDER BY date ASC")
    suspend fun getCheckInTrendByName(name: String): List<CheckInTrend>

    // 获取恋爱日记类型的打卡记录（特殊用途）
    @Query("SELECT * FROM checkins WHERE type = 'LOVE_DIARY' ORDER BY createdAt DESC")
    fun getLoveDiaryRecords(): Flow<List<CheckIn>>

    // 获取指定日期范围内的恋爱日记记录
    @Query("SELECT * FROM checkins WHERE type = 'LOVE_DIARY' AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getLoveDiaryRecordsBetweenDates(startDate: String, endDate: String): List<CheckIn>

    // 获取恋爱日记的最新记录
    @Query("SELECT * FROM checkins WHERE type = 'LOVE_DIARY' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestLoveDiaryRecord(): CheckIn?
}