package com.love.diary.viewmodel

import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.AppRepository
import com.love.diary.presentation.viewmodel.StatisticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

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
    fun `loadStatistics should use latest record per day and carry forward missing days`() = runTest {
        val today = LocalDate.now()
        val config = createConfig(today)

        val moods = listOf(
            createMoodEntity(
                date = today.minusDays(6).toString(),
                moodType = MoodType.HAPPY,
                updatedAt = 1_000L
            ),
            // newer record for the same date, should be used
            createMoodEntity(
                date = today.minusDays(6).toString(),
                moodType = MoodType.ANGRY,
                updatedAt = 2_000L
            ),
            createMoodEntity(
                date = today.minusDays(4).toString(),
                moodType = MoodType.HAPPY,
                updatedAt = 3_000L
            ),
            createMoodEntity(
                date = today.minusDays(2).toString(),
                moodType = MoodType.HAPPY,
                updatedAt = 4_000L
            )
        )

        whenever(repository.getAppConfig()).thenReturn(config)
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(moods))

        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(StatisticsViewModel.ContentState.CONTENT, state.contentState)
        assertFalse(state.isLoading)
        assertEquals(3, state.totalRecords) // unique days with records
        assertEquals(MoodType.HAPPY, state.topMood)
        val expectedAverage = String.format("%.1f", (-1 + 1 + 1) / 3f)
        assertEquals(expectedAverage, state.averageMood)

        // Trend should carry forward after the first data point
        val firstValue = state.moodTrend.first { it.first == today.minusDays(6).toString() }.second
        assertEquals(-1, firstValue)
        val carriedValue = state.moodTrend.first { it.first == today.minusDays(5).toString() }.second
        assertEquals(-1, carriedValue)
    }

    @Test
    fun `loadStatistics should mark empty when there is no data`() = runTest {
        val today = LocalDate.now()
        val config = createConfig(today)

        whenever(repository.getAppConfig()).thenReturn(config)
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))

        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(StatisticsViewModel.ContentState.EMPTY, state.contentState)
        assertEquals(0, state.totalRecords)
        assertEquals("0.0", state.averageMood)
        assertTrue(state.moodTrend.isEmpty())
    }

    @Test
    fun `updateTimeRange should update selected days`() = runTest {
        val today = LocalDate.now()
        val config = createConfig(today)
        whenever(repository.getAppConfig()).thenReturn(config)
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))

        viewModel = StatisticsViewModel(repository)
        advanceUntilIdle()

        viewModel.updateTimeRange(30)
        advanceUntilIdle()

        assertEquals(30, viewModel.uiState.value.selectedDays)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    private fun createMoodEntity(
        date: String,
        moodType: MoodType,
        updatedAt: Long,
        createdAt: Long = updatedAt - 100
    ): DailyMoodEntity {
        return DailyMoodEntity(
            id = 0L,
            date = date,
            dayIndex = 1,
            moodTypeCode = moodType.code,
            moodScore = moodType.score,
            moodText = null,
            hasText = false,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun createConfig(today: LocalDate) = AppConfigEntity(
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
}
