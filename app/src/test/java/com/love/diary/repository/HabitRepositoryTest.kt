package com.love.diary.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.HabitDao
import com.love.diary.data.database.dao.UnifiedCheckInDao
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import com.love.diary.data.model.HabitType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.model.UnifiedCheckInConfig
import com.love.diary.habit.DefaultHabitRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for HabitRepository bridge pattern implementation.
 * 
 * Tests the dual-write strategy where check-ins are written to both
 * the legacy Habit system and the new UnifiedCheckIn system.
 */
class HabitRepositoryTest {
    
    private lateinit var repository: DefaultHabitRepository
    private lateinit var database: LoveDatabase
    private lateinit var habitDao: HabitDao
    private lateinit var unifiedCheckInDao: UnifiedCheckInDao
    
    @Before
    fun setup() {
        database = mock()
        habitDao = mock()
        unifiedCheckInDao = mock()
        
        whenever(database.habitDao()).thenReturn(habitDao)
        whenever(database.unifiedCheckInDao()).thenReturn(unifiedCheckInDao)
        
        repository = DefaultHabitRepository(database)
    }
    
    /**
     * Test that insertHabit creates both a Habit entry and a UnifiedCheckInConfig.
     */
    @Test
    fun `insertHabit creates habit and unified config`() = runTest {
        // Given
        val habit = Habit(
            id = 1L,
            name = "Morning Exercise",
            description = "Exercise every morning",
            buttonLabel = "打卡",
            type = HabitType.POSITIVE,
            startDate = "2024-01-01",
            icon = "🏃",
            color = "#6200EE",
            tags = "fitness,morning"
        )
        
        whenever(habitDao.insertHabit(any())).thenReturn(1L)
        whenever(unifiedCheckInDao.getCheckInConfigByName(any())).thenReturn(null)
        whenever(unifiedCheckInDao.insertCheckInConfig(any())).thenReturn(1L)
        
        // When
        val habitId = repository.insertHabit(habit)
        
        // Then
        assertEquals(1L, habitId)
        verify(habitDao).insertHabit(habit)
        verify(unifiedCheckInDao).getCheckInConfigByName("Morning Exercise")
        verify(unifiedCheckInDao).insertCheckInConfig(any())
    }
    
    /**
     * Test that insertHabit doesn't create duplicate UnifiedCheckInConfig if one exists.
     */
    @Test
    fun `insertHabit does not create duplicate config`() = runTest {
        // Given
        val habit = Habit(
            id = 1L,
            name = "Morning Exercise",
            type = HabitType.POSITIVE,
            startDate = "2024-01-01"
        )
        
        val existingConfig = UnifiedCheckInConfig(
            id = 1L,
            name = "Morning Exercise",
            type = CheckInType.HABIT,
            buttonLabel = "打卡"
        )
        
        whenever(habitDao.insertHabit(any())).thenReturn(1L)
        whenever(unifiedCheckInDao.getCheckInConfigByName("Morning Exercise")).thenReturn(existingConfig)
        
        // When
        val habitId = repository.insertHabit(habit)
        
        // Then
        assertEquals(1L, habitId)
        verify(habitDao).insertHabit(habit)
        verify(unifiedCheckInDao).getCheckInConfigByName("Morning Exercise")
        verify(unifiedCheckInDao, never()).insertCheckInConfig(any())
    }
    
    /**
     * Test that updateHabit updates both habit and config.
     */
    @Test
    fun `updateHabit updates both systems`() = runTest {
        // Given
        val habit = Habit(
            id = 1L,
            name = "Morning Exercise",
            description = "Updated description",
            type = HabitType.POSITIVE,
            startDate = "2024-01-01"
        )
        
        val existingConfig = UnifiedCheckInConfig(
            id = 1L,
            name = "Morning Exercise",
            type = CheckInType.HABIT,
            buttonLabel = "打卡"
        )
        
        whenever(unifiedCheckInDao.getCheckInConfigByName("Morning Exercise")).thenReturn(existingConfig)
        
        // When
        repository.updateHabit(habit)
        
        // Then
        verify(habitDao).updateHabit(habit)
        verify(unifiedCheckInDao).updateCheckInConfig(any())
    }
    
    /**
     * Test that checkInHabit writes to both systems (dual-write).
     */
    @Test
    fun `checkInHabit dual-writes to both systems`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now().toString()
        val habit = Habit(
            id = habitId,
            name = "Morning Exercise",
            type = HabitType.POSITIVE,
            currentCount = 5,
            startDate = "2024-01-01",
            isCompletedToday = false
        )
        
        whenever(habitDao.getHabitById(habitId)).thenReturn(habit)
        whenever(habitDao.getTodaysRecord(habitId, today)).thenReturn(null)
        whenever(unifiedCheckInDao.insertCheckIn(any())).thenReturn(1L)
        whenever(habitDao.insertHabitRecord(any())).thenReturn(1L)
        
        // When
        val result = repository.checkInHabit(habitId, "morning")
        
        // Then
        assertTrue(result)
        
        // Verify UnifiedCheckIn was created
        verify(unifiedCheckInDao).insertCheckIn(argThat { checkIn ->
            checkIn.name == "Morning Exercise" &&
            checkIn.type == CheckInType.HABIT &&
            checkIn.tag == "morning" &&
            checkIn.count == 6 &&
            checkIn.isCompleted
        })
        
        // Verify HabitRecord was created
        verify(habitDao).insertHabitRecord(argThat { record ->
            record.habitId == habitId &&
            record.count == 6 &&
            record.note == "morning"
        })
        
        // Verify Habit was updated
        verify(habitDao).updateHabit(argThat { updatedHabit ->
            updatedHabit.currentCount == 6 &&
            updatedHabit.isCompletedToday
        })
    }
    
    /**
     * Test that checkInHabit prevents duplicate check-ins on the same day.
     */
    @Test
    fun `checkInHabit prevents duplicate check-ins`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now().toString()
        val habit = Habit(
            id = habitId,
            name = "Morning Exercise",
            type = HabitType.POSITIVE,
            currentCount = 5,
            startDate = "2024-01-01"
        )
        
        val existingRecord = HabitRecord(
            id = 1L,
            habitId = habitId,
            count = 5,
            date = today
        )
        
        whenever(habitDao.getHabitById(habitId)).thenReturn(habit)
        whenever(habitDao.getTodaysRecord(habitId, today)).thenReturn(existingRecord)
        
        // When
        val result = repository.checkInHabit(habitId, null)
        
        // Then
        assertFalse(result)
        verify(unifiedCheckInDao, never()).insertCheckIn(any())
        verify(habitDao, never()).insertHabitRecord(any())
    }
    
    /**
     * Test countdown habit type calculation.
     */
    @Test
    fun `checkInHabit handles countdown habit type`() = runTest {
        // Given
        val habitId = 1L
        val today = LocalDate.now().toString()
        val targetDate = LocalDate.now().plusDays(10).toString()
        
        val habit = Habit(
            id = habitId,
            name = "Exam Countdown",
            type = HabitType.COUNTDOWN,
            targetDate = targetDate,
            currentCount = 15,
            startDate = "2024-01-01"
        )
        
        whenever(habitDao.getHabitById(habitId)).thenReturn(habit)
        whenever(habitDao.getTodaysRecord(habitId, today)).thenReturn(null)
        whenever(unifiedCheckInDao.insertCheckIn(any())).thenReturn(1L)
        whenever(habitDao.insertHabitRecord(any())).thenReturn(1L)
        
        // When
        val result = repository.checkInHabit(habitId, null)
        
        // Then
        assertTrue(result)
        
        // Verify count is calculated as days until target (should be 10)
        verify(unifiedCheckInDao).insertCheckIn(argThat { checkIn ->
            checkIn.count == 10
        })
    }
    
    /**
     * Test that deleteHabit soft-deletes in both systems.
     */
    @Test
    fun `deleteHabit deactivates both systems`() = runTest {
        // Given
        val habitId = 1L
        val habit = Habit(
            id = habitId,
            name = "Old Habit",
            type = HabitType.POSITIVE,
            startDate = "2024-01-01"
        )
        
        val config = UnifiedCheckInConfig(
            id = 1L,
            name = "Old Habit",
            type = CheckInType.HABIT,
            buttonLabel = "打卡"
        )
        
        whenever(habitDao.getHabitById(habitId)).thenReturn(habit)
        whenever(unifiedCheckInDao.getCheckInConfigByName("Old Habit")).thenReturn(config)
        
        // When
        repository.deleteHabit(habitId)
        
        // Then
        verify(habitDao).deactivateHabit(habitId)
        verify(unifiedCheckInDao).deactivateCheckInConfig(config.id)
    }
    
    /**
     * Test reading from UnifiedCheckIn system.
     */
    @Test
    fun `getCheckInHistoryFromUnified returns unified check-ins`() = runTest {
        // Given
        val habitId = 1L
        val habit = Habit(
            id = habitId,
            name = "Morning Exercise",
            type = HabitType.POSITIVE,
            startDate = "2024-01-01"
        )
        
        val checkIns = listOf(
            UnifiedCheckIn(
                id = 1L,
                name = "Morning Exercise",
                type = CheckInType.HABIT,
                date = "2024-01-01",
                count = 1,
                isCompleted = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UnifiedCheckIn(
                id = 2L,
                name = "Morning Exercise",
                type = CheckInType.HABIT,
                date = "2024-01-02",
                count = 2,
                isCompleted = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        
        whenever(habitDao.getHabitById(habitId)).thenReturn(habit)
        whenever(unifiedCheckInDao.getCheckInsByName("Morning Exercise")).thenReturn(flowOf(checkIns))
        
        // When
        val result = repository.getCheckInHistoryFromUnified(habitId)
        
        // Then
        assertEquals(2, result.size)
        assertEquals("Morning Exercise", result[0].name)
        assertEquals(CheckInType.HABIT, result[0].type)
    }
    
    /**
     * Test reading from legacy system.
     */
    @Test
    fun `getCheckInHistoryFromLegacy returns habit records`() = runTest {
        // Given
        val habitId = 1L
        val records = listOf(
            HabitRecord(id = 1L, habitId = habitId, count = 1, date = "2024-01-01", createdAt = System.currentTimeMillis()),
            HabitRecord(id = 2L, habitId = habitId, count = 2, date = "2024-01-02", createdAt = System.currentTimeMillis())
        )
        
        whenever(habitDao.getHabitRecordsFlow(habitId)).thenReturn(flowOf(records))
        
        // When
        val result = repository.getCheckInHistoryFromLegacy(habitId)
        
        // Then
        assertEquals(2, result.size)
        assertEquals(habitId, result[0].habitId)
    }
}
