// presentation/viewmodel/HistoryViewModel.kt
package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _moodRecords = MutableStateFlow(emptyList<DailyMoodEntity>())
    val moodRecords: StateFlow<List<DailyMoodEntity>>
            = _moodRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true

            // 获取配置以获取正确的startDate
            val config = repository.getAppConfig()
            val startDateStr = config?.startDate
            
            // 从DailyMood数据库获取所有记录
            repository.getRecentMoods(365).collect { moodRecords ->
                // 如果需要，可以计算dayIndex
                val records = if (startDateStr != null) {
                    moodRecords.map { mood ->
                        // dayIndex应该已经存储在数据库中，但以防万一，我们可以重新计算
                        val dayIndex = try {
                            val startDate = LocalDate.parse(startDateStr)
                            val moodDate = LocalDate.parse(mood.date)
                            java.time.temporal.ChronoUnit.DAYS.between(startDate, moodDate).toInt() + 1
                        } catch (e: Exception) {
                            mood.dayIndex // 使用数据库中存储的值
                        }
                        
                        mood.copy(dayIndex = dayIndex)
                    }
                } else {
                    moodRecords
                }
                
                _moodRecords.value = records
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadHistory()
    }
}