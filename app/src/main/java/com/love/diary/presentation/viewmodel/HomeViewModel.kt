package com.love.diary.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.database.entities.DailyMoodEntity
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
    val todayMoodDate: String? = null,
    val selectedImageUri: String? = null,
    val showAnniversaryPopup: Boolean = false,
    val anniversaryMessage: String = "",
    val showOtherMoodDialog: Boolean = false,
    val otherMoodText: String = "",
    val isLoading: Boolean = true,
    val coupleName: String? = null,
    val avatarUri: String? = null,
    val partnerAvatarUri: String? = null,
    val startDate: String = "",
    val currentDateDisplay: String = "",
    val todayDate: String = "",
    val currentStreak: Int = 0,
    val currentCheckInConfig: String = "异地恋日记", // 添加当前打卡配置名称
    val recentTenMoods: List<DailyMoodEntity> = emptyList(), // 最近10条心情记录
    val isDescriptionEditing: Boolean = false,
    val descriptionError: String? = null
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
    
    // 从DailyMood数据库获取最新的心情数据
    private suspend fun loadTodayMoodData() {
        val today = LocalDate.now().toString()
        
        // 从DailyMood数据库获取今天的心情记录
        val todayMood = repository.getTodayMood()
        
        // 更新UI状态
        _uiState.update { state ->
            if (todayMood != null && todayMood.date == today) {
                val moodType = MoodType.fromCode(todayMood.moodTypeCode)
                state.copy(
                    todayMood = moodType,
                    todayMoodText = todayMood.moodText,
                    todayMoodDate = todayMood.date,
                    selectedImageUri = todayMood.singleImageUri,
                    otherMoodText = todayMood.moodText ?: "",
                    isDescriptionEditing = todayMood.moodText.isNullOrBlank(),
                    descriptionError = null
                )
            } else {
                state.copy(
                    todayMood = null,
                    todayMoodText = null,
                    todayMoodDate = null,
                    selectedImageUri = null,
                    otherMoodText = "",
                    isDescriptionEditing = true,
                    descriptionError = null
                )
            }
        }
    }
    
    // 加载最近10条心情记录
    private suspend fun loadRecentTenMoods() {
        val recentMoods = repository.getRecentNMoods(10)
        _uiState.update { state ->
            state.copy(recentTenMoods = recentMoods)
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
                        avatarUri = it.reservedText1,
                        partnerAvatarUri = it.reservedText2,
                        startDate = it.startDate
                    )
                }
            }
            
            // 从DailyMood数据库获取数据
            loadTodayMoodData()
            loadRecentTenMoods()
            
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
                        todayDate = todayStr,
                        currentStreak = currentStreak,
                        isLoading = false
                    )
                }
                
                checkAnniversary(dayIndex)
            } ?: run {
                _uiState.update { it.copy(isLoading = false, todayDate = todayStr, currentDateDisplay = "今天：$todayStr（$dayOfWeek）") }
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
                        avatarUri = it.reservedText1,
                        partnerAvatarUri = it.reservedText2,
                        dayIndex = dayIndex,
                        dayDisplay = dayDisplay,
                        todayDate = todayStr,
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
        val recentMoods = repository.getRecentNMoods(30)
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

    fun selectMood(moodType: MoodType, moodText: String? = null) {
        viewModelScope.launch {
            val textToSave = moodText?.takeIf { it.isNotBlank() }
            val imageUri = _uiState.value.selectedImageUri

            repository.saveTodayMood(moodType, textToSave, imageUri)

            _uiState.update {
                it.copy(
                    todayMood = moodType,
                    todayMoodText = textToSave,
                    todayMoodDate = LocalDate.now().toString(),
                    otherMoodText = textToSave ?: "",
                    selectedImageUri = imageUri,
                    showOtherMoodDialog = false,
                    isDescriptionEditing = textToSave.isNullOrBlank(),
                    descriptionError = null
                )
            }

            loadRecentTenMoods()
            val currentStreak = calculateCurrentStreak()
            _uiState.update { it.copy(currentStreak = currentStreak) }
        }
    }

    fun updateSelectedMood(moodType: MoodType) {
        // 只更新选中的心情，不保存到数据库
        _uiState.update {
            it.copy(
                todayMood = moodType,
                isDescriptionEditing = true,
                descriptionError = null
            )
        }
    }

    fun saveOtherMood(text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                val imageUri = _uiState.value.selectedImageUri
                // Save to DailyMood database
                repository.saveTodayMood(MoodType.OTHER, text, imageUri)
                
                _uiState.update { state ->
                    state.copy(
                        todayMood = MoodType.OTHER,
                        todayMoodText = text,
                        selectedImageUri = imageUri,
                        showOtherMoodDialog = false,
                        otherMoodText = text,
                        todayMoodDate = LocalDate.now().toString(),
                        isDescriptionEditing = false,
                        descriptionError = null
                    )
                }
                
                // Reload recent moods and streak
                loadRecentTenMoods()
                val currentStreak = calculateCurrentStreak()
                _uiState.update { it.copy(currentStreak = currentStreak) }
            }
        }
    }
    
    fun updateOtherMoodText(text: String) {
        _uiState.update { it.copy(otherMoodText = text, descriptionError = null) }
    }

    fun updateSelectedImage(uri: String?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun updateAvatar(isPartner: Boolean = false, uri: String) {
        viewModelScope.launch {
            val config = repository.getAppConfig() ?: return@launch
            val updatedConfig = config.copy(
                reservedText1 = if (isPartner) config.reservedText1 else uri,
                reservedText2 = if (isPartner) uri else config.reservedText2,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveAppConfig(updatedConfig)
            _uiState.update {
                it.copy(
                    avatarUri = updatedConfig.reservedText1,
                    partnerAvatarUri = updatedConfig.reservedText2
                )
            }
        }
    }

    fun enterDescriptionEditMode() {
        _uiState.update { state ->
            state.copy(
                isDescriptionEditing = true,
                otherMoodText = state.todayMoodText.orEmpty(),
                descriptionError = null
            )
        }
    }

    fun cancelDescriptionEdit() {
        _uiState.update { state ->
            state.copy(
                isDescriptionEditing = false,
                otherMoodText = state.todayMoodText.orEmpty(),
                descriptionError = null
            )
        }
    }

    /**
     * Save today's mood description with optional default text supplied by the UI layer.
     * @param text user input text
     * @param defaultText text to use when the user input is blank (typically from localized resources)
     */
    fun saveDescription(text: String, defaultText: String? = null) {
        viewModelScope.launch {
            val currentMood = _uiState.value.todayMood ?: return@launch
            val imageUri = _uiState.value.selectedImageUri
            val finalText = when {
                text.isNotBlank() -> text
                defaultText != null -> defaultText
                else -> null
            }
            runCatching {
                repository.saveTodayMood(currentMood, finalText, imageUri)
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(
                        todayMoodText = finalText,
                        otherMoodText = finalText ?: "",
                        selectedImageUri = imageUri,
                        todayMoodDate = LocalDate.now().toString(),
                        isDescriptionEditing = false,
                        descriptionError = null
                    )
                }
                // 刷新最近的心情记录和连续记录天数
                loadRecentTenMoods()
                val currentStreak = calculateCurrentStreak()
                _uiState.update { it.copy(currentStreak = currentStreak) }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        descriptionError = "保存失败，请重试",
                        isDescriptionEditing = true
                    )
                }
            }
        }
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
