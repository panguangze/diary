package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.model.MoodAggregation
import com.love.diary.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Period for chart data aggregation
 */
enum class ChartPeriod {
    WEEK,
    MONTH,
    YEAR
}

/**
 * UI State for Stats screen
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: ChartPeriod = ChartPeriod.WEEK,
    val chartData: List<MoodAggregation> = emptyList(),
    
    // Stats cards
    val totalRecordedDays: Int = 0,
    val consecutiveDays: Int = 0,
    val averageMoodScore: Double = 0.0,
    
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load stats
                loadStats()
                
                // Load chart data for default period (week)
                loadChartData(ChartPeriod.WEEK)
                
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(error = "加载数据失败") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private suspend fun loadStats() {
        try {
            val totalDays = checkInRepository.getTotalRecordedDays()
            val streak = checkInRepository.calculateConsecutiveStreak()
            
            _uiState.update { state ->
                state.copy(
                    totalRecordedDays = totalDays,
                    consecutiveDays = streak
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun loadChartData(period: ChartPeriod) {
        try {
            val periodString = when (period) {
                ChartPeriod.WEEK -> "week"
                ChartPeriod.MONTH -> "month"
                ChartPeriod.YEAR -> "year"
            }
            
            val data = checkInRepository.getMoodAggregationForPeriod(periodString)
            
            // Calculate average mood score
            val avgScore = if (data.isNotEmpty()) {
                data.map { it.avgScore }.average()
            } else {
                0.0
            }
            
            _uiState.update { state ->
                state.copy(
                    chartData = data,
                    averageMoodScore = avgScore
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * User selects a different time period for the chart
     */
    fun selectPeriod(period: ChartPeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPeriod = period, isLoading = true) }
            loadChartData(period)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        loadInitialData()
    }
}
