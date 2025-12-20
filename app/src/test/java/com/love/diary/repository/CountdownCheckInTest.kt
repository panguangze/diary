package com.love.diary.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.UnifiedCheckInDao
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.CountdownMode
import com.love.diary.data.model.UnifiedCheckInConfig
import com.love.diary.data.repository.CheckInRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for countdown check-in functionality in CheckInRepository.
 * 
 * Tests both day countdown and check-in countdown modes:
 * - Day countdown: Automatically decrements based on calendar days
 * - Check-in countdown: Progress changes only when user checks in
 */
class CountdownCheckInTest {
    
    private lateinit var repository: CheckInRepository
    private lateinit var database: LoveDatabase
    private lateinit var unifiedCheckInDao: UnifiedCheckInDao
    
    @Before
    fun setup() {
        database = mock()
        unifiedCheckInDao = mock()
        
        whenever(database.unifiedCheckInDao()).thenReturn(unifiedCheckInDao)
        
        repository = CheckInRepository(database)
    }
    
    /**
     * Test day countdown calculation with future target date
     */
    @Test
    fun testCalculateDaysRemaining_futureDate() = runTest {
        // Target date is 10 days from now
        val targetDate = LocalDate.now().plusDays(10).toString()
        
        val daysRemaining = repository.calculateDaysRemaining(targetDate)
        
        assertEquals(10, daysRemaining)
    }
    
    /**
     * Test day countdown calculation with past target date
     */
    @Test
    fun testCalculateDaysRemaining_pastDate() = runTest {
        // Target date was 5 days ago
        val targetDate = LocalDate.now().minusDays(5).toString()
        
        val daysRemaining = repository.calculateDaysRemaining(targetDate)
        
        assertEquals(0, daysRemaining) // Should return 0 for past dates
    }
    
    /**
     * Test day countdown calculation for today
     */
    @Test
    fun testCalculateDaysRemaining_today() = runTest {
        val targetDate = LocalDate.now().toString()
        
        val daysRemaining = repository.calculateDaysRemaining(targetDate)
        
        assertEquals(0, daysRemaining)
    }
    
    /**
     * Test check-in countdown remaining calculation
     */
    @Test
    fun testGetCheckInCountdownRemaining() = runTest {
        val config = UnifiedCheckInConfig(
            id = 1,
            name = "Test Countdown",
            type = CheckInType.CHECKIN_COUNTDOWN,
            countdownMode = CountdownMode.CHECKIN_COUNTDOWN,
            countdownTarget = 30,
            countdownProgress = 10
        )
        
        val remaining = repository.getCheckInCountdownRemaining(config)
        
        assertEquals(20, remaining)
    }
    
    /**
     * Test check-in countdown when progress exceeds target
     */
    @Test
    fun testGetCheckInCountdownRemaining_exceededTarget() = runTest {
        val config = UnifiedCheckInConfig(
            id = 1,
            name = "Test Countdown",
            type = CheckInType.CHECKIN_COUNTDOWN,
            countdownMode = CountdownMode.CHECKIN_COUNTDOWN,
            countdownTarget = 30,
            countdownProgress = 35 // Progress exceeds target
        )
        
        val remaining = repository.getCheckInCountdownRemaining(config)
        
        assertEquals(0, remaining) // Should not return negative
    }
    
    /**
     * Test day countdown progress calculation
     */
    @Test
    fun testGetCountdownProgress_dayCountdown() = runTest {
        val startDate = LocalDate.now().minusDays(5).toString()
        val targetDate = LocalDate.now().plusDays(5).toString()
        
        val config = UnifiedCheckInConfig(
            id = 1,
            name = "Test Day Countdown",
            type = CheckInType.DAY_COUNTDOWN,
            startDate = startDate,
            targetDate = targetDate,
            countdownMode = CountdownMode.DAY_COUNTDOWN
        )
        
        val progress = repository.getCountdownProgress(config)
        
        // 5 days elapsed out of 10 total days = 50%
        assertEquals(50f, progress, 0.1f)
    }
    
    /**
     * Test check-in countdown progress calculation
     */
    @Test
    fun testGetCountdownProgress_checkInCountdown() = runTest {
        val config = UnifiedCheckInConfig(
            id = 1,
            name = "Test Check-in Countdown",
            type = CheckInType.CHECKIN_COUNTDOWN,
            countdownMode = CountdownMode.CHECKIN_COUNTDOWN,
            countdownTarget = 20,
            countdownProgress = 5
        )
        
        val progress = repository.getCountdownProgress(config)
        
        // 5 check-ins out of 20 = 25%
        assertEquals(25f, progress, 0.1f)
    }
    
    /**
     * Test check-in countdown progress when completed
     */
    @Test
    fun testGetCountdownProgress_completed() = runTest {
        val config = UnifiedCheckInConfig(
            id = 1,
            name = "Test Countdown",
            type = CheckInType.CHECKIN_COUNTDOWN,
            countdownMode = CountdownMode.CHECKIN_COUNTDOWN,
            countdownTarget = 30,
            countdownProgress = 30
        )
        
        val progress = repository.getCountdownProgress(config)
        
        assertEquals(100f, progress, 0.1f)
    }
    
    /**
     * Test invalid countdown mode returns 0 progress
     */
    @Test
    fun testGetCountdownProgress_nullMode() = runTest {
        val config = UnifiedCheckInConfig(
            id = 1,
            name = "Test Config",
            type = CheckInType.CUSTOM,
            countdownMode = null
        )
        
        val progress = repository.getCountdownProgress(config)
        
        assertEquals(0f, progress, 0.1f)
    }
}
