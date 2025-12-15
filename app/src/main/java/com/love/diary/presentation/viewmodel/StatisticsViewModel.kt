// presentation/viewmodel/StatisticsViewModel.kt
package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    data class StatisticsUiState(
        val selectedDays: Int = 7,
        val totalRecords: Int = 0,
        val averageMood: String = "0.0",
        val topMood: MoodType? = null,
        val moodStats: Map<MoodType, Int> = emptyMap(),
        val moodTrend: List<Pair<String, Int>> = emptyList(),
        val checkInTrend: List<com.love.diary.data.model.CheckInTrend> = emptyList(),
        val currentViewType: ViewType = ViewType.MOOD, // 当前查看的统计类型
        val isLoading: Boolean = true,
        val primaryCheckInName: String = "异地恋日记",
        val contentState: ContentState = ContentState.LOADING,
        val errorMessage: String? = null
    )

    enum class ViewType {
        MOOD,      // 心情统计
        CHECK_IN   // 打卡统计
    }

    enum class ContentState {
        LOADING,
        CONTENT,
        EMPTY,
        ERROR
    }

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    private var moodTrendJob: Job? = null

    init {
        loadStatistics()
    }

    fun refresh() {
        when (_uiState.value.currentViewType) {
            ViewType.MOOD -> loadStatistics()
            ViewType.CHECK_IN -> loadCheckInStatistics()
        }
    }

    fun updateTimeRange(days: Int) {
        _uiState.update { 
            it.copy(
                selectedDays = days, 
                isLoading = true,
                contentState = ContentState.LOADING,
                errorMessage = null
            ) 
        }
        when (_uiState.value.currentViewType) {
            ViewType.MOOD -> loadStatistics()
            ViewType.CHECK_IN -> loadCheckInStatistics()
        }
    }

    fun switchToCheckInTrend(checkInName: String? = null) {
        _uiState.update { 
            it.copy(
                currentViewType = ViewType.CHECK_IN,
                isLoading = true,
                contentState = ContentState.LOADING,
                errorMessage = null
            ) 
        }
        moodTrendJob?.cancel()
        loadCheckInStatistics(checkInName)
    }

    fun switchToMoodTrend() {
        _uiState.update { 
            it.copy(
                currentViewType = ViewType.MOOD,
                isLoading = true,
                contentState = ContentState.LOADING,
                errorMessage = null
            ) 
        }
        loadStatistics()
    }

    private fun loadStatistics() {
        moodTrendJob?.cancel()
        moodTrendJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, contentState = ContentState.LOADING, errorMessage = null) }
            try {
                repository.getRecentMoods(limit = 120).collect { moodRecords ->
                    val days = _uiState.value.selectedDays
                    val endDate = LocalDate.now()
                    val startDate = endDate.minusDays(days.toLong() - 1)

                    val recordsInRange = moodRecords.filter { record ->
                        runCatching { LocalDate.parse(record.date) }
                            .getOrNull()
                            ?.let { !it.isBefore(startDate) && !it.isAfter(endDate) }
                            ?: false
                    }

                    val latestPerDay = recordsInRange.groupBy { it.date }.mapValues { entry ->
                        entry.value.maxByOrNull { record ->
                            val updated = record.updatedAt.takeIf { it > 0 } ?: 0L
                            if (updated > 0) updated else record.createdAt
                        } ?: entry.value.first()
                    }

                    val dates = (0 until days).map { startDate.plusDays(it) }
                    val rawValues = dates.map { date ->
                        latestPerDay[date.toString()]?.let { record ->
                            moodScoreForTrend(MoodType.fromCode(record.moodTypeCode))
                        }
                    }

                    val filledTrend = mutableListOf<Pair<String, Int>>()
                    var lastFilled: Int? = null
                    dates.zip(rawValues).forEach { (date, raw) ->
                        val value = raw ?: lastFilled
                        if (raw != null || lastFilled != null) {
                            lastFilled = value
                        }
                        if (value != null) {
                            filledTrend.add(date.toString() to value)
                        }
                    }

                    val totalRecords = rawValues.count { it != null }
                    val moodStats = latestPerDay.values
                        .groupBy { MoodType.fromCode(it.moodTypeCode) }
                        .mapValues { it.value.size }
                    val averageMood = if (totalRecords > 0) {
                        val sum = rawValues.filterNotNull().sum()
                        String.format("%.1f", sum.toFloat() / totalRecords)
                    } else "0.0"
                    val topMood = moodStats.maxByOrNull { it.value }?.key

                    val newContentState = if (totalRecords == 0) ContentState.EMPTY else ContentState.CONTENT

                    _uiState.update { state ->
                        state.copy(
                            totalRecords = totalRecords,
                            averageMood = averageMood,
                            topMood = topMood,
                            moodStats = moodStats,
                            moodTrend = filledTrend,
                            checkInTrend = emptyList(),
                            isLoading = false,
                            contentState = newContentState,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        contentState = ContentState.ERROR,
                        errorMessage = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    private fun loadCheckInStatistics(checkInName: String? = null) {
        viewModelScope.launch {
            try {
                val days = _uiState.value.selectedDays
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(days.toLong() - 1)
                val config = repository.getAppConfig()
                val targetName = checkInName ?: config?.coupleName ?: _uiState.value.primaryCheckInName
                // 获取打卡趋势数据
                val checkInTrend = repository.getCheckInTrendByName(targetName)
                val filteredTrend = checkInTrend.filter {
                    runCatching { LocalDate.parse(it.date) }
                        .getOrNull()
                        ?.let { date -> !date.isBefore(startDate) && !date.isAfter(endDate) }
                        ?: false
                }
                
                // 计算打卡统计
                val totalRecords = filteredTrend.sumOf { it.count }
                
                _uiState.update { state ->
                    state.copy(
                        totalRecords = totalRecords,
                        checkInTrend = filteredTrend,
                        moodTrend = emptyList(), // 清空心情趋势数据
                        isLoading = false,
                        primaryCheckInName = targetName,
                        contentState = if (totalRecords == 0) ContentState.EMPTY else ContentState.CONTENT,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        contentState = ContentState.ERROR,
                        errorMessage = e.message ?: "加载失败"
                    ) 
                }
                // 可以添加错误处理逻辑
            }
        }
    }

    private fun moodScoreForTrend(moodType: MoodType): Int {
        // Normalize to PRD-defined scoring for the trend chart (+1 / 0 / -1),
        // which differs from the raw MoodType.score values.
        return when (moodType) {
            MoodType.HAPPY, MoodType.SATISFIED -> 1
            MoodType.NORMAL, MoodType.OTHER -> 0
            MoodType.SAD, MoodType.ANGRY -> -1
        }
    }
}
