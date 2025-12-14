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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    data class StatisticsUiState(
        val selectedDays: Int = 30,
        val totalRecords: Int = 0,
        val averageMood: String = "0.0",
        val topMood: MoodType? = null,
        val moodStats: Map<MoodType, Int> = emptyMap(),
        val moodTrend: List<Pair<String, Int>> = emptyList(),
        val checkInTrend: List<com.love.diary.data.model.CheckInTrend> = emptyList(),
        val currentViewType: ViewType = ViewType.MOOD, // 当前查看的统计类型
        val isLoading: Boolean = true
    )

    enum class ViewType {
        MOOD,      // 心情统计
        CHECK_IN   // 打卡统计
    }

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun updateTimeRange(days: Int) {
        _uiState.update { it.copy(selectedDays = days, isLoading = true) }
        loadStatistics()
    }

    fun switchToCheckInTrend(checkInName: String) {
        _uiState.update { 
            it.copy(
                currentViewType = ViewType.CHECK_IN,
                isLoading = true
            ) 
        }
        loadCheckInStatistics(checkInName)
    }

    fun switchToMoodTrend() {
        _uiState.update { 
            it.copy(
                currentViewType = ViewType.MOOD,
                isLoading = true
            ) 
        }
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val days = _uiState.value.selectedDays

            // 计算开始日期
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(days.toLong() - 1)

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDateStr = startDate.format(dateFormatter)
            val endDateStr = endDate.format(dateFormatter)

            // 从统一打卡系统获取"异地恋日记"记录
            val checkIns = repository.getRecentCheckInsByName("异地恋日记", days * 2) // 获取更多记录以确保覆盖日期范围
            
            // 过滤日期范围内的记录，使用LocalDate进行比较
            val records = checkIns.filter { checkIn ->
                try {
                    val checkInDate = LocalDate.parse(checkIn.date)
                    !checkInDate.isBefore(startDate) && !checkInDate.isAfter(endDate)
                } catch (e: Exception) {
                    false // 如果日期解析失败，排除该记录
                }
            }
            
            val totalRecords = records.size

            // 计算心情统计
            val moodStats = mutableMapOf<MoodType, Int>()
            var totalScore = 0

            records.forEach { checkIn ->
                // 使用工具函数将tag映射到MoodType
                val moodType = MoodType.fromTag(checkIn.tag)
                
                moodStats[moodType] = moodStats.getOrDefault(moodType, 0) + 1
                totalScore += moodType.score
            }

            // 计算平均心情
            val averageMood = if (totalRecords > 0) {
                String.format("%.1f", totalScore.toFloat() / totalRecords)
            } else "0.0"

            // 找到最多的心情
            val topMood = moodStats.maxByOrNull { it.value }?.key

            // 获取心情趋势数据
            val trendData = records.map { checkIn ->
                val moodType = MoodType.fromTag(checkIn.tag)
                checkIn.date to moodType.score
            }

            _uiState.update { state ->
                state.copy(
                    totalRecords = totalRecords,
                    averageMood = averageMood,
                    topMood = topMood,
                    moodStats = moodStats,
                    moodTrend = trendData,
                    checkInTrend = emptyList(), // 清空打卡趋势数据
                    isLoading = false
                )
            }
        }
    }

    private fun loadCheckInStatistics(checkInName: String) {
        viewModelScope.launch {
            try {
                // 获取打卡趋势数据
                val checkInTrend = repository.getCheckInTrendByName(checkInName)
                
                // 计算打卡统计
                val totalRecords = checkInTrend.size
                
                _uiState.update { state ->
                    state.copy(
                        totalRecords = totalRecords,
                        checkInTrend = checkInTrend,
                        moodTrend = emptyList(), // 清空心情趋势数据
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                // 可以添加错误处理逻辑
            }
        }
    }
}