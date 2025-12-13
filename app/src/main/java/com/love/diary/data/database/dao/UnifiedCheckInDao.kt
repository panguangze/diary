package com.love.diary.data.database.dao

import androidx.room.*
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.model.UnifiedCheckInConfig
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.CheckInTrend
import kotlinx.coroutines.flow.Flow

@Dao
interface UnifiedCheckInDao {
    
    // UnifiedCheckIn operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: UnifiedCheckIn): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIns(checkIns: List<UnifiedCheckIn>): List<Long>
    
    @Update
    suspend fun updateCheckIn(checkIn: UnifiedCheckIn)
    
    @Delete
    suspend fun deleteCheckIn(checkIn: UnifiedCheckIn)
    
    @Query("DELETE FROM unified_checkins WHERE id = :id")
    suspend fun deleteCheckInById(id: Long)
    
    @Query("SELECT * FROM unified_checkins WHERE name = :name")
    fun getCheckInsByName(name: String): Flow<List<UnifiedCheckIn>>
    
    @Query("SELECT * FROM unified_checkins WHERE date = :date")
    fun getCheckInsByDate(date: String): Flow<List<UnifiedCheckIn>>
    
    @Query("SELECT * FROM unified_checkins WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getCheckInsBetweenDates(startDate: String, endDate: String): Flow<List<UnifiedCheckIn>>
    
    @Query("SELECT * FROM unified_checkins WHERE type = :type")
    fun getCheckInsByType(type: CheckInType): Flow<List<UnifiedCheckIn>>
    
    @Query("SELECT * FROM unified_checkins WHERE type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getCheckInsByTypeAndDateRange(type: CheckInType, startDate: String, endDate: String): Flow<List<UnifiedCheckIn>>
    
    @Query("SELECT * FROM unified_checkins WHERE date = :date AND name = :name LIMIT 1")
    suspend fun getCheckInByDateAndName(date: String, name: String): UnifiedCheckIn?
    
    @Query("SELECT DISTINCT type FROM unified_checkins")
    fun getUniqueCheckInTypes(): Flow<List<CheckInType>>
    
    @Query("SELECT COUNT(*) FROM unified_checkins WHERE name = :name")
    suspend fun getCheckInCountByName(name: String): Int
    
    @Query("SELECT * FROM unified_checkins WHERE name = :name ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentCheckInsByName(name: String, limit: Int): List<UnifiedCheckIn>
    
    @Query("SELECT date, COUNT(*) as count FROM unified_checkins WHERE name = :name GROUP BY date ORDER BY date")
    suspend fun getCheckInTrendByName(name: String): List<CheckInTrend>
    
    // Love diary specific queries
    @Query("SELECT * FROM unified_checkins WHERE type = 'LOVE_DIARY' ORDER BY date DESC")
    fun getLoveDiaryRecords(): Flow<List<UnifiedCheckIn>>
    
    @Query("SELECT * FROM unified_checkins WHERE type = 'LOVE_DIARY' AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getLoveDiaryRecordsBetweenDates(startDate: String, endDate: String): List<UnifiedCheckIn>
    
    @Query("SELECT * FROM unified_checkins WHERE type = 'LOVE_DIARY' ORDER BY date DESC LIMIT 1")
    suspend fun getLatestLoveDiaryRecord(): UnifiedCheckIn?
    
    // UnifiedCheckInConfig operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckInConfig(config: UnifiedCheckInConfig): Long
    
    @Update
    suspend fun updateCheckInConfig(config: UnifiedCheckInConfig)
    
    @Query("UPDATE unified_checkin_configs SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCheckInConfig(id: Long)
    
    @Query("SELECT * FROM unified_checkin_configs WHERE isActive = 1")
    fun getAllCheckInConfigs(): Flow<List<UnifiedCheckInConfig>>
    
    @Query("SELECT * FROM unified_checkin_configs WHERE type = :type AND isActive = 1")
    fun getCheckInConfigsByType(type: CheckInType): Flow<List<UnifiedCheckInConfig>>
    
    @Query("SELECT * FROM unified_checkin_configs WHERE id = :id AND isActive = 1")
    suspend fun getCheckInConfigById(id: Long): UnifiedCheckInConfig?
    
    @Query("SELECT * FROM unified_checkin_configs WHERE name = :name AND isActive = 1 LIMIT 1")
    suspend fun getCheckInConfigByName(name: String): UnifiedCheckInConfig?
}