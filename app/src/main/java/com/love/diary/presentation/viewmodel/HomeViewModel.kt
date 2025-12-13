package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.firstOrNull
import com.love.diary.data.model.MoodType
import com.love.diary.data.model.EventType
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
    val currentStreak: Int = 0,
    val currentCheckInConfig: String = "异地恋日记" // 添加当前打卡配置名称
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
    
    // 从统一打卡系统获取最新的异地恋日记打卡数据
    private suspend fun loadSpecialHabitData() {
        // 从统一打卡系统获取"异地恋日记"的最新记录
        val checkInRecords = repository.getRecentCheckInsByName("异地恋日记", 1)
        if (checkInRecords.isNotEmpty()) {
            val latestRecord = checkInRecords.first()
            // 尝试将打卡标签映射到MoodType
            val moodType = when (latestRecord.tag) {
                "开心" -> MoodType.HAPPY
                "满足" -> MoodType.SATISFIED
                "正常" -> MoodType.NORMAL
                "失落" -> MoodType.SAD
                "生气" -> MoodType.ANGRY
                else -> MoodType.OTHER
            }
            
            // 更新UI状态
            _uiState.update { state ->
                state.copy(
                    todayMood = moodType,
                    todayMoodText = if (moodType == MoodType.OTHER) latestRecord.tag else null
                )
            }
        } else {
            // 如果没有找到记录，则将心情设置为null
            _uiState.update { state ->
                state.copy(
                    todayMood = null,
                    todayMoodText = null
                )
            }
        }
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
            
            // 从特殊打卡事项获取数据，而不是从心情数据库
            loadSpecialHabitData()
            
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
                // 获取特殊打卡事项并进行打卡
                val allHabits = repository.getAllHabits().firstOrNull() ?: emptyList()
                val specialHabit = allHabits.find { 
                    it.name == "我们的名字" || 
                    (uiState.value.coupleName != null && it.name == uiState.value.coupleName)
                }
                
                if (specialHabit != null) {
                    // 获取心情标签对应的文本
                    val moodTag = when (moodType) {
                        MoodType.HAPPY -> "开心"
                        MoodType.SATISFIED -> "满足"
                        MoodType.NORMAL -> "正常"
                        MoodType.SAD -> "失落"
                        MoodType.ANGRY -> "生气"
                        else -> "其它"
                    }
                    
                    // 对异地恋日记进行打卡 - 使用固定的打卡配置名称
                    repository.checkInHabit("异地恋日记", moodTag)
                    
                    // 更新UI状态
                    _uiState.update {
                        it.copy(
                            todayMood = moodType,
                            todayMoodText = null
                        )
                    }
                }
            }
        }
    }

    fun saveOtherMood(text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                // 获取特殊打卡事项并进行打卡
                val allHabits = repository.getAllHabits().firstOrNull() ?: emptyList()
                val specialHabit = allHabits.find { 
                    it.name == "我们的名字" || 
                    (uiState.value.coupleName != null && it.name == uiState.value.coupleName)
                }
                
                if (specialHabit != null) {
                    // 对异地恋日记进行打卡 - 使用固定的打卡配置名称
                    repository.checkInHabit("异地恋日记", text)
                    
                    _uiState.update { state ->
                        state.copy(
                            todayMood = MoodType.OTHER,
                            todayMoodText = text,
                            showOtherMoodDialog = false,
                            otherMoodText = ""
                        )
                    }
                }
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
    
    fun setCurrentCheckInConfig(configName: String) {
        _uiState.update { it.copy(currentCheckInConfig = configName) }
        // 根据配置名称加载相应的数据
        if (configName == "异地恋日记") {
            // 加载异地恋日记相关数据
            loadInitialData()
        } else {
            // 对于其他打卡事项，我们可以加载通用的打卡数据
            viewModelScope.launch {
                val today = LocalDate.now()
                val todayStr = today.toString()
                val dayOfWeek = today.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
                
                _uiState.update { state ->
                    state.copy(
                        currentDateDisplay = "今天：$todayStr（$dayOfWeek）",
                        dayIndex = 0, // 为其他打卡事项重置为0
                        dayDisplay = "",
                        todayMood = null,
                        todayMoodText = null,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun calculateDayIndex(startDate: String, targetDate: String): Int {
        val start = LocalDate.parse(startDate)
        val target = LocalDate.parse(targetDate)
        // 使用ChronoUnit计算天数差异，这能更准确地处理所有日期边界情况
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, target) + 1
        return daysBetween.toInt()
    }
    
    // === 新增功能：使用新的事件模型 ===
    
    // 获取今天的事件
    suspend fun getTodaysEvents(): List<com.love.diary.data.model.Event> {
        val today = LocalDate.now().toString()
        return repository.getEventsForDate(today)
    }
    
    // 创建新事件
    suspend fun createEvent(name: String, type: EventType, moodType: MoodType? = null, tag: String? = null): Long {
        val event = com.love.diary.data.model.Event(
            name = name,
            type = type,
            moodType = moodType,
            tag = tag
        )
        return repository.createEvent(event)
    }
    
    // 获取活动事件配置
    fun getActiveEventConfigs() = repository.getActiveEventConfigs()
    
    // 创建事件配置
    suspend fun createEventConfig(
        name: String, 
        type: EventType, 
        description: String? = null,
        buttonLabel: String = "记录",
        icon: String = "📝",
        color: String = "#6200EE"
    ): Long {
        val config = com.love.diary.data.model.EventConfig(
            name = name,
            type = type,
            description = description,
            buttonLabel = buttonLabel,
            icon = icon,
            color = color
        )
        return repository.createEventConfig(config)
    }
}
