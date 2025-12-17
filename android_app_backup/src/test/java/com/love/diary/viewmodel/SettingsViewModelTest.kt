package com.love.diary.viewmodel

import com.love.diary.data.backup.DataBackupManager
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.repository.AppRepository
import com.love.diary.presentation.viewmodel.SettingsViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for SettingsViewModel
 * Tests configuration management and settings updates
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var repository: AppRepository
    private lateinit var backupManager: DataBackupManager
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        backupManager = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        whenever(repository.getAppConfig()).thenReturn(null)
        
        // When
        viewModel = SettingsViewModel(repository, backupManager)
        
        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadSettings should update UI state with config data`() = runTest {
        // Given
        val testConfig = AppConfigEntity(
            id = 1,
            startDate = "2024-01-01",
            startTimeMinutes = 0,
            coupleName = "Test Couple",
            partnerNickname = "Sweetie",
            showMoodTip = true,
            showStreak = false,
            showAnniversary = true,
            darkMode = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(repository.getAppConfig()).thenReturn(testConfig)
        
        // When
        viewModel = SettingsViewModel(repository, backupManager)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("2024-01-01", state.startDate)
        assertEquals("Test Couple", state.coupleName)
        assertEquals("Sweetie", state.partnerNickname)
        assertTrue(state.showMoodTip)
        assertFalse(state.showStreak)
        assertTrue(state.showAnniversary)
        assertEquals(true, state.darkMode)
    }

    @Test
    fun `setDarkMode should update config and UI state`() = runTest {
        // Given
        val initialConfig = AppConfigEntity(
            id = 1,
            startDate = "2024-01-01",
            startTimeMinutes = 0,
            coupleName = null,
            partnerNickname = null,
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            darkMode = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(repository.getAppConfig()).thenReturn(initialConfig)
        
        viewModel = SettingsViewModel(repository, backupManager)
        advanceUntilIdle()
        
        // When
        viewModel.setDarkMode(true)
        advanceUntilIdle()
        
        // Then
        verify(repository).updateAppConfig(any())
        assertEquals(true, viewModel.uiState.value.darkMode)
    }

    @Test
    fun `toggleMoodTip should update config`() = runTest {
        // Given
        val config = AppConfigEntity(
            id = 1,
            startDate = "2024-01-01",
            startTimeMinutes = 0,
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(repository.getAppConfig()).thenReturn(config)
        
        viewModel = SettingsViewModel(repository, backupManager)
        advanceUntilIdle()
        
        // When
        viewModel.toggleMoodTip(false)
        advanceUntilIdle()
        
        // Then
        verify(repository).updateAppConfig(any())
        assertFalse(viewModel.uiState.value.showMoodTip)
    }

    @Test
    fun `updateCoupleName should update config and state`() = runTest {
        // Given
        val config = AppConfigEntity(
            id = 1,
            startDate = "2024-01-01",
            startTimeMinutes = 0,
            coupleName = "Old Name",
            showMoodTip = true,
            showStreak = true,
            showAnniversary = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(repository.getAppConfig()).thenReturn(config)
        
        viewModel = SettingsViewModel(repository, backupManager)
        advanceUntilIdle()
        
        // When
        viewModel.updateCoupleName("New Name")
        advanceUntilIdle()
        
        // Then
        verify(repository).updateAppConfig(any())
        assertEquals("New Name", viewModel.uiState.value.coupleName)
    }

    @Test
    fun `loadSettings should handle null config gracefully`() = runTest {
        // Given
        whenever(repository.getAppConfig()).thenReturn(null)
        
        // When
        viewModel = SettingsViewModel(repository, backupManager)
        advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.startDate)
        assertNull(state.coupleName)
        assertNull(state.partnerNickname)
    }
}
