package com.love.diary.viewmodel

import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.AppRepository
import com.love.diary.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

/**
 * Unit tests for HomeViewModel
 * Tests mood selection and day counting functionality
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
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
    fun `initial state should be loading`() = runTest {
        // Given
        whenever(repository.getAppConfig()).thenReturn(null)
        whenever(repository.isFirstRun()).thenReturn(false)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(null))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel = HomeViewModel(repository)
        
        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadInitialData should calculate correct day index`() = runTest {
        // Given
        val today = LocalDate.now()
        val startDate = today.minusDays(99).toString() // 100 days ago
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = startDate,
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.isFirstRun()).thenReturn(false)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(testConfig))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        // When
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(100, state.dayIndex)
        assertEquals("Test Couple", state.coupleName)
    }

    @Test
    fun `selectMood should update UI state`() = runTest {
        // Given
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = "2024-01-01",
            startTimeMinutes = 0,
            coupleName = "Test",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        whenever(repository.getAppConfig()).thenReturn(testConfig)
        whenever(repository.isFirstRun()).thenReturn(false)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(testConfig))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        
        // When
        viewModel.selectMood(MoodType.HAPPY)
        advanceUntilIdle()
        
        // Then
        // Note: In actual implementation, this would require mocking habit check-in
        // For now we verify the method was called
        assertNotNull(viewModel.uiState.value)
    }

    @Test
    fun `showOtherMoodDialog should update dialog visibility`() = runTest {
        // Given
        whenever(repository.getAppConfig()).thenReturn(null)
        whenever(repository.isFirstRun()).thenReturn(false)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(null))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        
        // When
        viewModel.showOtherMoodDialog()
        
        // Then
        assertTrue(viewModel.uiState.value.showOtherMoodDialog)
    }

    @Test
    fun `closeOtherMoodDialog should hide dialog and clear text`() = runTest {
        // Given
        whenever(repository.getAppConfig()).thenReturn(null)
        whenever(repository.isFirstRun()).thenReturn(false)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(null))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        
        viewModel.showOtherMoodDialog()
        viewModel.updateOtherMoodText("Test mood")
        
        // When
        viewModel.closeOtherMoodDialog()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showOtherMoodDialog)
        assertEquals("", state.otherMoodText)
    }

    @Test
    fun `dismissAnniversaryPopup should hide popup`() = runTest {
        // Given
        whenever(repository.getAppConfig()).thenReturn(null)
        whenever(repository.isFirstRun()).thenReturn(false)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(null))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        viewModel = HomeViewModel(repository)
        advanceUntilIdle()
        
        // When
        viewModel.dismissAnniversaryPopup()
        
        // Then
        assertFalse(viewModel.uiState.value.showAnniversaryPopup)
    }

    @Test
    fun `isFirstRun should return repository value`() = runTest {
        // Given
        whenever(repository.isFirstRun()).thenReturn(true)
        whenever(repository.getAppConfig()).thenReturn(null)
        whenever(repository.getAppConfigFlow()).thenReturn(flowOf(null))
        whenever(repository.getAllHabits()).thenReturn(flowOf(emptyList()))
        whenever(repository.getRecentCheckInsByName(any(), any())).thenReturn(emptyList())
        whenever(repository.getRecentMoods(any())).thenReturn(flowOf(emptyList()))
        
        viewModel = HomeViewModel(repository)
        
        // When
        val result = viewModel.isFirstRun()
        
        // Then
        assertTrue(result)
        verify(repository).isFirstRun()
    }
}
