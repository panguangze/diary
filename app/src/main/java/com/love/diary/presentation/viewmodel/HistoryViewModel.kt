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

            // 获取配置以获取正确的默认打卡名称
            val config = repository.getAppConfig()
            val defaultCheckInName = config?.coupleName ?: "异地恋日记"
            val startDateStr = config?.startDate
            
            // 从统一打卡系统获取对应名称的记录
            repository.getCheckInsByName(defaultCheckInName).collect { checkIns ->
                // 将UnifiedCheckIn转换为DailyMoodEntity用于显示
                val moodRecords = checkIns.mapNotNull { checkIn ->
                    // 使用工具函数将tag映射到MoodType
                    val moodType = MoodType.fromTag(checkIn.tag)
                    
                    // 计算dayIndex
                    val dayIndex = if (startDateStr != null) {
                        try {
                            val startDate = LocalDate.parse(startDateStr)
                            val checkInDate = LocalDate.parse(checkIn.date)
                            java.time.temporal.ChronoUnit.DAYS.between(startDate, checkInDate).toInt() + 1
                        } catch (e: Exception) {
                            0
                        }
                    } else {
                        0
                    }
                    
                    // 对于OTHER类型，moodText存储自定义文本（如果有）；对于其他类型，moodText为null
                    val hasText = moodType == MoodType.OTHER && !checkIn.tag.isNullOrBlank()
                    val moodText = if (hasText) checkIn.tag else null
                    
                    DailyMoodEntity(
                        id = checkIn.id,
                        date = checkIn.date,
                        dayIndex = dayIndex,
                        moodTypeCode = moodType.code,
                        moodScore = moodType.score,
                        moodText = moodText,
                        hasText = hasText,
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