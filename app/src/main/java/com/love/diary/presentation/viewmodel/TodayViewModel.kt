package com.love.diary.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.model.MoodType
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.repository.AppRepository
import com.love.diary.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * UI State for Today screen
 */
data class TodayUiState(
    val isLoading: Boolean = true,
    val coupleName: String? = null,
    val startDate: String = "",
    val loveDays: Int = 0,
    val dateRangeText: String = "",
    
    // Mood selection
    val selectedMood: MoodType? = null,
    val comfortingMessage: String = "",
    val moodNote: String = "",
    val selectedImageUri: String? = null,
    
    // Stats
    val totalRecordedDays: Int = 0,
    val consecutiveDays: Int = 0,
    val mostCommonMood: MoodType? = null,
    
    // Recent moods (last 30 days)
    val recentMoods: List<UnifiedCheckIn> = emptyList(),
    
    val isSaving: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val checkInRepository: CheckInRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load app config
                val config = appRepository.getAppConfig()
                config?.let {
                    val loveDays = calculateLoveDays(it.startDate)
                    val dateRangeText = formatDateRange(it.startDate)
                    
                    _uiState.update { state ->
                        state.copy(
                            coupleName = it.coupleName,
                            startDate = it.startDate,
                            loveDays = loveDays,
                            dateRangeText = dateRangeText
                        )
                    }
                }
                
                // Load today's check-in
                val todayCheckIn = checkInRepository.getTodayCheckIn()
                todayCheckIn?.let {
                    _uiState.update { state ->
                        state.copy(
                            selectedMood = it.moodType,
                            comfortingMessage = it.moodType?.feedbackText ?: "",
                            moodNote = it.note ?: "",
                            selectedImageUri = it.attachmentUri
                        )
                    }
                }
                
                // Load stats
                loadStats()
                
                // Load recent moods
                loadRecentMoods()
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private suspend fun loadStats() {
        try {
            val totalDays = checkInRepository.getTotalRecordedDays()
            val streak = checkInRepository.calculateConsecutiveStreak()
            val mostCommon = checkInRepository.getMostCommonMoodInLastDays(30)
            
            _uiState.update { state ->
                state.copy(
                    totalRecordedDays = totalDays,
                    consecutiveDays = streak,
                    mostCommonMood = mostCommon
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun loadRecentMoods() {
        try {
            val recentMoods = checkInRepository.getLastNDaysCheckIns(30)
            _uiState.update { state ->
                state.copy(recentMoods = recentMoods)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * User selects a mood
     */
    fun selectMood(mood: MoodType) {
        _uiState.update { state ->
            state.copy(
                selectedMood = mood,
                comfortingMessage = mood.feedbackText
            )
        }
    }
    
    /**
     * User updates the mood note
     */
    fun updateMoodNote(note: String) {
        _uiState.update { state ->
            state.copy(moodNote = note)
        }
    }
    
    /**
     * User selects an image
     */
    fun selectImage(uri: Uri) {
        _uiState.update { state ->
            state.copy(selectedImageUri = uri.toString())
        }
    }
    
    /**
     * User clears the selected image
     */
    fun clearImage() {
        _uiState.update { state ->
            state.copy(selectedImageUri = null)
        }
    }
    
    /**
     * Save today's mood entry
     */
    fun save() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.selectedMood == null) {
                _uiState.update { it.copy(saveError = "请选择心情") }
                return@launch
            }
            
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            
            try {
                checkInRepository.checkInLoveDiary(
                    name = "恋爱日记",
                    moodType = currentState.selectedMood,
                    note = currentState.moodNote.ifBlank { null },
                    attachmentUri = currentState.selectedImageUri
                )
                
                // Reload stats and recent moods after saving
                loadStats()
                loadRecentMoods()
                
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        saveError = "保存失败，请重试"
                    )
                }
            }
        }
    }
    
    /**
     * Calculate the number of days since the start date
     */
    private fun calculateLoveDays(startDate: String): Int {
        return try {
            val start = LocalDate.parse(startDate)
            val today = LocalDate.now()
            java.time.temporal.ChronoUnit.DAYS.between(start, today).toInt() + 1
        } catch (e: Exception) {
            1
        }
    }
    
    /**
     * Format the date range text
     */
    private fun formatDateRange(startDate: String): String {
        return try {
            val start = LocalDate.parse(startDate)
            val today = LocalDate.now()
            "$startDate - ${today}"
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(saveError = null) }
    }
}
