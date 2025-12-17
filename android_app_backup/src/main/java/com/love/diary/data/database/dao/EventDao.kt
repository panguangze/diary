package com.love.diary.data.database.dao

import androidx.room.*
import com.love.diary.data.model.Event
import com.love.diary.data.model.EventConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    // Event相关操作
    @Query("SELECT * FROM events WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getEventsByDate(date: String): List<Event>

    @Query("SELECT * FROM events WHERE type = :eventType ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    fun getEventsByType(eventType: com.love.diary.data.model.EventType, limit: Int, offset: Int): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE habitId = :habitId ORDER BY createdAt DESC")
    fun getEventsByHabitId(habitId: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getEventsBetweenDates(startDate: String, endDate: String): List<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>): List<Long>

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    // EventConfig相关操作
    @Query("SELECT * FROM event_configs WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllActiveConfigs(): Flow<List<EventConfig>>

    @Query("SELECT * FROM event_configs WHERE type = :eventType AND isActive = 1 ORDER BY updatedAt DESC")
    fun getConfigsByEventType(eventType: com.love.diary.data.model.EventType): Flow<List<EventConfig>>

    @Query("SELECT * FROM event_configs WHERE id = :id")
    suspend fun getConfigById(id: Long): EventConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: EventConfig): Long

    @Update
    suspend fun updateConfig(config: EventConfig)

    @Query("UPDATE event_configs SET isActive = 0 WHERE id = :id") // 软删除
    suspend fun deactivateConfig(id: Long)

    @Delete
    suspend fun deleteConfig(config: EventConfig)
}