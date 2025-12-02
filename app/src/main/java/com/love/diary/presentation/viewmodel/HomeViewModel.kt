package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val dayIndex: Int = 0,
    val dayDisplay: String = "",
    val todayMood: MoodType? = null,
    val todayMoodText: String? = null,
    val showAnniversaryPopup: Boolean = false,
    val anniversaryMessage: String = "",
    val showOtherMoodDialog: Boolean = false,
    val otherMoodText: String = "",
    val isLoading: Boolean = true,
    val coupleName: String? = null,
    val startDate: String = "",
    val currentDateDisplay: String = "",
    val currentStreak: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    val repository: AppRepository  // 改为public，以便在MainActivity中访问
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
        observeConfigChanges()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val config = repository.getAppConfig()
            config?.let {
                _uiState.update { state ->
                    state.copy(
                        coupleName = it.coupleName,
                        startDate = it.startDate
                    )
                }
            }
            
            val todayMood = repository.getTodayMood()
            todayMood?.let {
                _uiState.update { state ->
                    state.copy(
                        todayMood = MoodType.fromCode(it.moodTypeCode),
                        todayMoodText = it.moodText
                    )
                }
            }
            
            val today = LocalDate.now()
            val todayStr = today.toString()
            val dayOfWeek = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
            
            config?.let {
                val dayIndex = calculateDayIndex(it.startDate, todayStr)
                val dayDisplay = repository.getDayDisplay(dayIndex)
                val currentStreak = calculateCurrentStreak() // 计算连续记录天数
                
                _uiState.update { state ->
                    state.copy(
                        dayIndex = dayIndex,
                        dayDisplay = dayDisplay,
                        currentDateDisplay = "今天：$todayStr（$dayOfWeek）",
                        currentStreak = currentStreak,
                        isLoading = false
                    )
                }
                
                checkAnniversary(dayIndex)
            } ?: run {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun observeConfigChanges() {
        viewModelScope.launch {
            repository.getAppConfigFlow().collect { config ->
                config?.let {
                    val today = LocalDate.now()
                    val todayStr = today.toString()
                    val dayOfWeek = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
                    
                    val dayIndex = calculateDayIndex(it.startDate, todayStr)
                    val dayDisplay = repository.getDayDisplay(dayIndex)
                    val currentStreak = calculateCurrentStreak()
                    
                    _uiState.update { state ->
                        state.copy(
                            coupleName = it.coupleName,
                            startDate = it.startDate,
                            dayIndex = dayIndex,
                            dayDisplay = dayDisplay,
                            currentStreak = currentStreak
                        )
                    }
                    
                    checkAnniversary(dayIndex)
                }
            }
        }
    }
    
    private suspend fun calculateCurrentStreak(): Int {
        // 获取最近的记录，计算连续记录天数
        val recentMoods = repository.getRecentMoods(30).firstOrNull() ?: emptyList()
        if (recentMoods.isEmpty()) return 0
        
        var streak = 0
        val today = LocalDate.now()
        
        // 从今天开始向前检查连续的记录天数
        for (i in 0 until 30) {
            val checkDate = today.minusDays(i.toLong())
            val checkDateStr = checkDate.toString()
            
            val hasRecord = recentMoods.any { mood -> mood.date == checkDateStr }
            if (hasRecord) {
                streak++
            } else {
                // 如果某一天没有记录，连续记录中断
                break
            }
        }
        
        return streak
    }

    fun selectMood(moodType: MoodType) {
        viewModelScope.launch {
            if (moodType == MoodType.OTHER) {
                _uiState.update { it.copy(showOtherMoodDialog = true) }
            } else {
                // 真实保存到数据库
                repository.saveTodayMood(moodType)

                // 更新UI状态
                _uiState.update {
                    it.copy(
                        todayMood = moodType,
                        todayMoodText = null
                    )
                }

                // 显示保存成功反馈
                // TODO: 可以使用Snackbar显示
            }
        }
    }

    fun saveOtherMood(text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                // 真实保存到数据库
                repository.saveTodayMood(MoodType.OTHER, text)

                _uiState.update { state ->
                    state.copy(
                        todayMood = MoodType.OTHER,
                        todayMoodText = text,
                        showOtherMoodDialog = false,
                        otherMoodText = ""
                    )
                }

                // 显示保存成功反馈
            }
        }
    }
    
    fun updateOtherMoodText(text: String) {
        _uiState.update { it.copy(otherMoodText = text) }
    }
    
    fun showOtherMoodDialog() {
        _uiState.update { it.copy(showOtherMoodDialog = true) }
    }
    
    fun closeOtherMoodDialog() {
        _uiState.update { it.copy(showOtherMoodDialog = false, otherMoodText = "") }
    }
    
    fun dismissAnniversaryPopup() {
        _uiState.update { it.copy(showAnniversaryPopup = false) }
    }
    
    private fun checkAnniversary(dayIndex: Int) {
        if (dayIndex % 100 == 0) {
            val message = when (dayIndex) {
                100 -> "🎉 今天是我们在一起的第 100 天！\n谢谢你一直在这段关系里这么认真。"
                200 -> "🎉 这是我们一起走过的第 2 个100天，\n期待下一个100天里，我们可以见到彼此更多次。"
                300 -> "🎉 300天的陪伴！\n每一个日夜都让我们的感情更加深厚。"
                else -> "🎉 今天是我们在一起的第 $dayIndex 天！\n感谢你一直以来的陪伴。"
            }
            
            _uiState.update { state ->
                state.copy(
                    showAnniversaryPopup = true,
                    anniversaryMessage = message
                )
            }
        }
    }
    
    suspend fun isFirstRun(): Boolean {
        return repository.isFirstRun()
    }
    
    private fun calculateDayIndex(startDate: String, targetDate: String): Int {
        val start = LocalDate.parse(startDate)
        val target = LocalDate.parse(targetDate)
        return start.until(target).days + 1
    }
}
