// presentation/viewmodel/HistoryViewModel.kt
package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _moodRecords = MutableStateFlow(emptyList<com.love.diary.data.database.entities.DailyMoodEntity>())
    val moodRecords: StateFlow<List<com.love.diary.data.database.entities.DailyMoodEntity>>
            = _moodRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true

            // 从统一打卡系统获取"异地恋日记"记录
            repository.getCheckInsByName("异地恋日记").collect { checkIns ->
                // 获取配置以计算dayIndex
                val config = repository.getAppConfig()
                val startDateStr = config?.startDate
                
                // 将UnifiedCheckIn转换为DailyMoodEntity用于显示
                val moodRecords = checkIns.mapNotNull { checkIn ->
                    // 使用工具函数将tag映射到MoodType
                    val moodType = com.love.diary.data.model.MoodType.fromTag(checkIn.tag)
                    
                    // 计算dayIndex
                    val dayIndex = if (startDateStr != null) {
                        try {
                            val startDate = java.time.LocalDate.parse(startDateStr)
                            val checkInDate = java.time.LocalDate.parse(checkIn.date)
                            java.time.temporal.ChronoUnit.DAYS.between(startDate, checkInDate).toInt() + 1
                        } catch (e: Exception) {
                            0
                        }
                    } else {
                        0
                    }
                    
                    com.love.diary.data.database.entities.DailyMoodEntity(
                        id = checkIn.id,
                        date = checkIn.date,
                        dayIndex = dayIndex,
                        moodTypeCode = moodType.code,
                        moodScore = moodType.score,
                        moodText = checkIn.tag,
                        hasText = checkIn.tag != null,
                        isAnniversary = false,
                        anniversaryType = null,
                        createdAt = checkIn.createdAt,
                        updatedAt = checkIn.updatedAt
                    )
                }
                _moodRecords.value = moodRecords
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadHistory()
    }
}