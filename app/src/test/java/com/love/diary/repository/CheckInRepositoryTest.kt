package com.love.diary.repository

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.UnifiedCheckInDao
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.MoodAggregation
import com.love.diary.data.model.MoodType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.repository.CheckInRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDate

/**
 * Unit tests for CheckInRepository stats calculations.
 * 
 * Tests the key business logic including:
 * - Consecutive streak calculation
 * - Total recorded days count
 * - Most common mood calculation
 * - Mood aggregations for charts
 */
class CheckInRepositoryTest {
    
    private lateinit var repository: CheckInRepository
    private lateinit var database: LoveDatabase
    private lateinit var dao: UnifiedCheckInDao
    
    @Before
    fun setup() {
        database = mock()
        dao = mock()
        
        whenever(database.unifiedCheckInDao()).thenReturn(dao)
        
        repository = CheckInRepository(database)
    }
    
    /**
     * Test streak calculation with consecutive days
     */
    @Test
    fun testConsecutiveStreak_withConsecutiveDays() = runTest {
        // Setup: 5 consecutive days of check-ins
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.toString()),
            createCheckIn(today.minusDays(1).toString()),
            createCheckIn(today.minusDays(2).toString()),
            createCheckIn(today.minusDays(3).toString()),
            createCheckIn(today.minusDays(4).toString())
        )
        
        whenever(dao.getLastNDaysCheckIns(365)).thenReturn(checkIns)
        
        // Execute
        val streak = repository.calculateConsecutiveStreak()
        
        // Verify
        assertEquals(5, streak)
    }
    
    /**
     * Test streak calculation with a gap in records
     */
    @Test
    fun testConsecutiveStreak_withGap() = runTest {
        // Setup: 3 consecutive days, then a gap, then more records
        val today = LocalDate.now()
        val checkIns = listOf(
            createCheckIn(today.toString()),
            createCheckIn(today.minusDays(1).toString()),
            createCheckIn(today.minusDays(2).toString()),
            // Gap on day 3
            createCheckIn(today.minusDays(4).toString()),
            createCheckIn(today.minusDays(5).toString())
        )
        
        whenever(dao.getLastNDaysCheckIns(365)).thenReturn(checkIns)
        
        // Execute
        val streak = repository.calculateConsecutiveStreak()
        
        // Verify - should only count the 3 consecutive days
        assertEquals(3, streak)
    }
    
    /**
     * Test streak calculation with no records
     */
    @Test
    fun testConsecutiveStreak_withNoRecords() = runTest {
        whenever(dao.getLastNDaysCheckIns(365)).thenReturn(emptyList())
        
        val streak = repository.calculateConsecutiveStreak()
        
        assertEquals(0, streak)
    }
    
    /**
     * Test streak calculation with only today's record
     */
    @Test
    fun testConsecutiveStreak_withOnlyToday() = runTest {
        val today = LocalDate.now()
        val checkIns = listOf(createCheckIn(today.toString()))
        
        whenever(dao.getLastNDaysCheckIns(365)).thenReturn(checkIns)
        
        val streak = repository.calculateConsecutiveStreak()
        
        assertEquals(1, streak)
    }
    
    /**
     * Test total recorded days count
     */
    @Test
    fun testGetTotalRecordedDays() = runTest {
        whenever(dao.getTotalRecordedDays()).thenReturn(42)
        
        val total = repository.getTotalRecordedDays()
        
        assertEquals(42, total)
        verify(dao).getTotalRecordedDays()
    }
    
    /**
     * Test most common mood calculation
     */
    @Test
    fun testGetMostCommonMood() = runTest {
        whenever(dao.getMostCommonMoodInLastDays(30)).thenReturn("HAPPY")
        
        val mood = repository.getMostCommonMoodInLastDays(30)
        
        assertEquals(MoodType.HAPPY, mood)
        verify(dao).getMostCommonMoodInLastDays(30)
    }
    
    /**
     * Test most common mood when no data exists
     */
    @Test
    fun testGetMostCommonMood_noData() = runTest {
        whenever(dao.getMostCommonMoodInLastDays(30)).thenReturn(null)
        
        val mood = repository.getMostCommonMoodInLastDays(30)
        
        assertNull(mood)
    }
    
    /**
     * Test mood aggregation for week period
     */
    @Test
    fun testGetMoodAggregation_week() = runTest {
        val today = LocalDate.now()
        val startDate = today.minusDays(6).toString()
        val endDate = today.toString()
        
        val aggregations = listOf(
            MoodAggregation(today.toString(), 4.5),
            MoodAggregation(today.minusDays(1).toString(), 3.2),
            MoodAggregation(today.minusDays(2).toString(), 4.0)
        )
        
        whenever(dao.getMoodAggregationByDateRange(startDate, endDate))
            .thenReturn(aggregations)
        
        val result = repository.getMoodAggregationForPeriod("week")
        
        assertEquals(3, result.size)
        assertEquals(4.5, result[0].avgScore, 0.01)
        verify(dao).getMoodAggregationByDateRange(startDate, endDate)
    }
    
    /**
     * Test mood aggregation for month period
     */
    @Test
    fun testGetMoodAggregation_month() = runTest {
        val today = LocalDate.now()
        val startDate = today.minusDays(29).toString()
        val endDate = today.toString()
        
        val aggregations = listOf(
            MoodAggregation(today.toString(), 4.0)
        )
        
        whenever(dao.getMoodAggregationByDateRange(startDate, endDate))
            .thenReturn(aggregations)
        
        val result = repository.getMoodAggregationForPeriod("month")
        
        assertEquals(1, result.size)
        verify(dao).getMoodAggregationByDateRange(startDate, endDate)
    }
    
    /**
     * Test mood aggregation for year period
     */
    @Test
    fun testGetMoodAggregation_year() = runTest {
        val today = LocalDate.now()
        val startDate = today.minusMonths(11).withDayOfMonth(1).toString()
        val endDate = today.toString()
        
        whenever(dao.getMoodAggregationByDateRange(startDate, endDate))
            .thenReturn(emptyList())
        
        val result = repository.getMoodAggregationForPeriod("year")
        
        assertEquals(0, result.size)
        verify(dao).getMoodAggregationByDateRange(startDate, endDate)
    }
    
    /**
     * Test mood aggregation with invalid period
     */
    @Test
    fun testGetMoodAggregation_invalidPeriod() = runTest {
        val result = repository.getMoodAggregationForPeriod("invalid")
        
        assertEquals(0, result.size)
        verifyNoInteractions(dao)
    }
    
    /**
     * Test getting today's check-in
     */
    @Test
    fun testGetTodayCheckIn() = runTest {
        val today = LocalDate.now().toString()
        val checkIn = createCheckIn(today)
        
        whenever(dao.getTodayCheckIn(today)).thenReturn(checkIn)
        
        val result = repository.getTodayCheckIn()
        
        assertNotNull(result)
        assertEquals(today, result?.date)
        verify(dao).getTodayCheckIn(today)
    }
    
    /**
     * Test getting today's check-in when none exists
     */
    @Test
    fun testGetTodayCheckIn_noData() = runTest {
        val today = LocalDate.now().toString()
        
        whenever(dao.getTodayCheckIn(today)).thenReturn(null)
        
        val result = repository.getTodayCheckIn()
        
        assertNull(result)
    }
    
    /**
     * Test check-in for love diary
     */
    @Test
    fun testCheckInLoveDiary() = runTest {
        val today = LocalDate.now().toString()
        
        whenever(dao.getCheckInByDateAndName(today, "恋爱日记")).thenReturn(null)
        whenever(dao.insertCheckIn(any())).thenReturn(1L)
        
        val id = repository.checkInLoveDiary(
            moodType = MoodType.HAPPY,
            note = "Test note",
            attachmentUri = "content://test"
        )
        
        assertEquals(1L, id)
        verify(dao).insertCheckIn(any())
    }
    
    /**
     * Test check-in replaces existing record for same day
     */
    @Test
    fun testCheckInLoveDiary_replacesExisting() = runTest {
        val today = LocalDate.now().toString()
        val existingCheckIn = createCheckIn(today, id = 100L)
        
        whenever(dao.getCheckInByDateAndName(today, "恋爱日记"))
            .thenReturn(existingCheckIn)
        whenever(dao.insertCheckIn(any())).thenReturn(101L)
        
        val id = repository.checkInLoveDiary(
            moodType = MoodType.SWEET,
            note = "New note"
        )
        
        verify(dao).deleteCheckInById(100L)
        verify(dao).insertCheckIn(any())
        assertEquals(101L, id)
    }
    
    // Helper function to create test check-ins
    private fun createCheckIn(
        date: String,
        id: Long = 1L,
        moodType: MoodType = MoodType.HAPPY
    ): UnifiedCheckIn {
        return UnifiedCheckIn(
            id = id,
            name = "恋爱日记",
            type = CheckInType.LOVE_DIARY,
            date = date,
            moodType = moodType,
            note = "Test note"
        )
    }
}
