package com.love.diary.viewmodel

import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.model.CheckInTrend
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.MoodType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.repository.AppRepository
import com.love.diary.presentation.viewmodel.StatisticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

/**
 * Unit tests for StatisticsViewModel
 * Tests mood statistics calculation functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var repository: AppRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadStatistics should calculate correct mood statistics with moodType field`() = runTest {
        // Given
        val today = LocalDate.now()
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = today.minusDays(30).toString(),
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Create test check-ins with moodType field set (not tag)
        val testCheckIns = listOf(
            UnifiedCheckIn(
                id = 1,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(5).toString(),
                moodType = MoodType.HAPPY,
                tag = null
            ),
            UnifiedCheckIn(
                id = 2,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(4).toString(),
                moodType = MoodType.HAPPY,
                tag = null
            ),
            UnifiedCheckIn(
                id = 3,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(3).toString(),
                moodType = MoodType.SATISFIED,
                tag = null
            ),
            UnifiedCheckIn(
                id = 4,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(2).toString(),
                moodType = MoodType.NORMAL,
                tag = null
            ),
            UnifiedCheckIn(
                id = 5,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(1).toString(),
                moodType = MoodType.SAD,
                tag = null
            )
        )

        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.getRecentCheckInsByName("Test Couple", 60)).thenReturn(testCheckIns)

        // When
        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(5, state.totalRecords)
        assertEquals(2, state.moodStats[MoodType.HAPPY]) // 2 HAPPY moods
        assertEquals(1, state.moodStats[MoodType.SATISFIED]) // 1 SATISFIED mood
        assertEquals(1, state.moodStats[MoodType.NORMAL]) // 1 NORMAL mood
        assertEquals(1, state.moodStats[MoodType.SAD]) // 1 SAD mood
        assertEquals(MoodType.HAPPY, state.topMood) // Most frequent is HAPPY
        
        // Calculate expected average dynamically
        val expectedAverage = (MoodType.HAPPY.score * 2 + 
                              MoodType.SATISFIED.score + 
                              MoodType.NORMAL.score + 
                              MoodType.SAD.score).toFloat() / 5
        assertEquals(String.format("%.1f", expectedAverage), state.averageMood)
    }

    @Test
    fun `loadStatistics should handle records with tag fallback when moodType is null`() = runTest {
        // Given
        val today = LocalDate.now()
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = today.minusDays(30).toString(),
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Create test check-ins with tag field set (legacy data)
        val testCheckIns = listOf(
            UnifiedCheckIn(
                id = 1,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(2).toString(),
                moodType = null,
                tag = MoodType.toTag(MoodType.HAPPY) // Using toTag for consistency
            ),
            UnifiedCheckIn(
                id = 2,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(1).toString(),
                moodType = null,
                tag = MoodType.toTag(MoodType.SATISFIED) // Using toTag for consistency
            )
        )

        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.getRecentCheckInsByName("Test Couple", 60)).thenReturn(testCheckIns)

        // When
        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.totalRecords)
        assertEquals(1, state.moodStats[MoodType.HAPPY])
        assertEquals(1, state.moodStats[MoodType.SATISFIED])
    }

    @Test
    fun `loadStatistics should handle empty records gracefully`() = runTest {
        // Given
        val today = LocalDate.now()
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = today.minusDays(30).toString(),
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.getRecentCheckInsByName("Test Couple", 60)).thenReturn(emptyList())

        // When
        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.totalRecords)
        assertEquals("0.0", state.averageMood)
        assertEquals(null, state.topMood)
        assertEquals(emptyMap<MoodType, Int>(), state.moodStats)
    }

    @Test
    fun `updateTimeRange should reload statistics with new date range`() = runTest {
        // Given
        val today = LocalDate.now()
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = today.minusDays(90).toString(),
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val testCheckIns = listOf(
            UnifiedCheckIn(
                id = 1,
                name = "Test Couple",
                type = CheckInType.LOVE_DIARY,
                date = today.minusDays(5).toString(),
                moodType = MoodType.HAPPY,
                tag = null
            )
        )

        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.getRecentCheckInsByName("Test Couple", 60)).thenReturn(testCheckIns)
        whenever(repository.getRecentCheckInsByName("Test Couple", 180)).thenReturn(testCheckIns)

        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.updateTimeRange(90)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(90, state.selectedDays)
        assertFalse(state.isLoading)
    }

    @Test
    fun `switchToCheckInTrend should change view type and load check-in statistics`() = runTest {
        // Given
        val today = LocalDate.now()
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = today.minusDays(30).toString(),
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val testCheckInTrend = listOf(
            CheckInTrend(date = today.minusDays(2).toString(), count = 2),
            CheckInTrend(date = today.minusDays(1).toString(), count = 1)
        )

        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.getRecentCheckInsByName("Test Couple", 60)).thenReturn(emptyList())
        whenever(repository.getCheckInTrendByName("Test Couple")).thenReturn(testCheckInTrend)

        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.switchToCheckInTrend()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(StatisticsViewModel.ViewType.CHECK_IN, state.currentViewType)
        assertNotNull(state.checkInTrend)
        assertFalse(state.isLoading)
    }

    @Test
    fun `switchToMoodTrend should change view type back to mood statistics`() = runTest {
        // Given
        val today = LocalDate.now()
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = today.minusDays(30).toString(),
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.getRecentCheckInsByName("Test Couple", 60)).thenReturn(emptyList())
        whenever(repository.getCheckInTrendByName("Test Couple")).thenReturn(emptyList())

        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        viewModel.switchToCheckInTrend()
        advanceUntilIdle()

        // When
        viewModel.switchToMoodTrend()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(StatisticsViewModel.ViewType.MOOD, state.currentViewType)
        assertFalse(state.isLoading)
    }
}
