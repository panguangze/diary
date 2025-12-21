package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.model.UnifiedCheckInConfig
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DASHBOARD_HISTORY_DAYS = 90L

data class CheckInUiState(
    val currentCheckInConfig: UnifiedCheckInConfig? = null,
    val checkInRecords: List<UnifiedCheckIn> = emptyList(),
    val allCheckInRecords: List<UnifiedCheckIn> = emptyList(), // All check-in records for dashboard
    val allCheckInConfigs: List<UnifiedCheckInConfig> = emptyList(),
    val checkInTypes: List<CheckInType> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val checkInRepository: CheckInRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CheckInUiState())
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()
    
    init {
        loadAllCheckInConfigs()
        loadCheckInTypes()
        loadAllCheckInRecords()
    }
    
    private fun loadAllCheckInConfigs() {
        viewModelScope.launch {
            checkInRepository.getAllCheckInConfigs().collect { configs ->
                _uiState.update { state ->
                    state.copy(
                        allCheckInConfigs = configs
                    )
                }
                
                // 如果当前没有选中的打卡配置，且存在配置，则默认选择第一个
                if (_uiState.value.currentCheckInConfig == null && configs.isNotEmpty()) {
                    selectCheckInConfig(configs.first())
                }
            }
        }
    }
    
    private fun loadAllCheckInRecords() {
        viewModelScope.launch {
            // Load check-ins from the last DASHBOARD_HISTORY_DAYS for dashboard
            val endDate = java.time.LocalDate.now().toString()
            val startDate = java.time.LocalDate.now().minusDays(DASHBOARD_HISTORY_DAYS).toString()
            checkInRepository.getCheckInsBetweenDates(startDate, endDate).collect { records ->
                _uiState.update { state ->
                    state.copy(allCheckInRecords = records)
                }
            }
        }
    }
    
    private fun loadCheckInTypes() {
        // Use all available check-in types from the enum
        // This ensures users always see all available types
        val allTypes = CheckInType.values().toList()
        
        _uiState.update { state ->
            state.copy(checkInTypes = allTypes)
        }
    }
    
    fun selectCheckInConfig(config: UnifiedCheckInConfig) {
        _uiState.update { state ->
            state.copy(currentCheckInConfig = config)
        }
        loadCheckInRecords(config.name)
    }
    
    private fun loadCheckInRecords(name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            checkInRepository.getCheckInsByName(name).collect { records ->
                _uiState.update { state ->
                    state.copy(
                        checkInRecords = records,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    // 通用打卡功能
    fun checkIn(
        name: String,
        type: CheckInType,
        moodType: MoodType? = null,
        tag: String? = null,
        tagColor: String? = null,
        note: String? = null,
        attachmentUri: String? = null,
        duration: Int? = null,
        rating: Int? = null,
        count: Int = 1,
        configId: Long? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkIn(
                name = name,
                type = type,
                moodType = moodType,
                tag = tag,
                tagColor = tagColor,
                note = note,
                attachmentUri = attachmentUri,
                duration = duration,
                rating = rating,
                count = count,
                configId = configId
            )
        }
    }
    
    fun checkInLoveDiary(
        name: String = "恋爱日记",
        moodType: MoodType,
        note: String? = null,
        attachmentUri: String? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInLoveDiary(
                name = name,
                moodType = moodType,
                note = note,
                attachmentUri = attachmentUri
            )
        }
    }
    
    fun checkInHabit(
        name: String,
        tag: String? = null,
        note: String? = null,
        attachmentUri: String? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInHabit(
                name = name,
                tag = tag,
                note = note,
                attachmentUri = attachmentUri
            )
        }
    }
    
    fun checkInExercise(
        name: String,
        note: String? = null,
        duration: Int? = null,
        rating: Int? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInExercise(
                name = name,
                note = note,
                duration = duration,
                rating = rating
            )
        }
    }
    
    fun checkInStudy(
        name: String,
        note: String? = null,
        duration: Int? = null,
        count: Int = 1
    ) {
        viewModelScope.launch {
            checkInRepository.checkInStudy(
                name = name,
                note = note,
                duration = duration,
                count = count
            )
        }
    }
    
    fun checkInWorkout(
        name: String,
        note: String? = null,
        duration: Int? = null,
        rating: Int? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInWorkout(
                name = name,
                note = note,
                duration = duration,
                rating = rating
            )
        }
    }
    
    fun checkInDiet(
        name: String,
        note: String? = null,
        tag: String? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInDiet(
                name = name,
                note = note,
                tag = tag
            )
        }
    }
    
    fun checkInMeditation(
        name: String,
        note: String? = null,
        duration: Int? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInMeditation(
                name = name,
                note = note,
                duration = duration
            )
        }
    }
    
    fun checkInReading(
        name: String,
        note: String? = null,
        duration: Int? = null,
        count: Int = 1
    ) {
        viewModelScope.launch {
            checkInRepository.checkInReading(
                name = name,
                note = note,
                duration = duration,
                count = count
            )
        }
    }
    
    fun checkInWater(
        name: String,
        count: Int = 1,
        note: String? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInWater(
                name = name,
                count = count,
                note = note
            )
        }
    }
    
    fun checkInSleep(
        name: String,
        duration: Int? = null,
        moodType: MoodType? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInSleep(
                name = name,
                duration = duration,
                moodType = moodType
            )
        }
    }
    
    fun checkInCustom(
        name: String,
        type: CheckInType = CheckInType.CUSTOM,
        note: String? = null,
        tag: String? = null,
        count: Int = 1
    ) {
        viewModelScope.launch {
            checkInRepository.checkInCustom(
                name = name,
                type = type,
                note = note,
                tag = tag,
                count = count
            )
        }
    }
    
    fun createCheckInConfig(config: UnifiedCheckInConfig) {
        viewModelScope.launch {
            checkInRepository.saveCheckInConfig(config)
        }
    }
    
    fun updateCheckInConfig(config: UnifiedCheckInConfig) {
        viewModelScope.launch {
            checkInRepository.updateCheckInConfig(config)
        }
    }
    
    fun deleteCheckInConfig(id: Long) {
        viewModelScope.launch {
            checkInRepository.deleteCheckInConfig(id)
        }
    }
    
    /**
     * Edit check-in config - only certain fields can be modified
     */
    fun editCheckInConfig(
        id: Long,
        name: String,
        icon: String,
        description: String?,
        reminderTime: String?,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val existingConfig = checkInRepository.getCheckInConfigById(id)
            existingConfig?.let {
                val updatedConfig = it.copy(
                    name = name,
                    icon = icon,
                    description = description,
                    reminderTime = reminderTime,
                    updatedAt = System.currentTimeMillis()
                )
                checkInRepository.updateCheckInConfig(updatedConfig)
            }
        }
    }
    
    // 获取指定日期范围内的打卡记录
    fun loadCheckInsBetweenDates(startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val records = checkInRepository.getCheckInsBetweenDates(startDate, endDate).first()
            _uiState.update { state ->
                state.copy(
                    checkInRecords = records,
                    isLoading = false
                )
            }
        }
    }
    
    // 获取指定类型和日期范围内的打卡记录
    fun loadCheckInsByTypeAndDateRange(type: CheckInType, startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val records = checkInRepository.getCheckInsByTypeAndDateRange(type, startDate, endDate).first()
            _uiState.update { state ->
                state.copy(
                    checkInRecords = records,
                    isLoading = false
                )
            }
        }
    }
    
    // 获取恋爱日记记录
    fun loadLoveDiaryRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            checkInRepository.getLoveDiaryRecords().collect { records ->
                _uiState.update { state ->
                    state.copy(
                        checkInRecords = records,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    // 获取最新的恋爱日记记录
    fun getLatestLoveDiaryRecord() = viewModelScope.launch {
        val latestRecord = checkInRepository.getLatestLoveDiaryRecord()
        // 可以更新UI状态或返回结果
    }

    // ========== 倒计时打卡相关方法 ==========

    /**
     * 创建天数倒计时
     * @param name 倒计时名称
     * @param targetDate 目标日期
     * @param description 描述
     * @param icon 图标
     * @param color 颜色
     * @param reminderTime 提醒时间
     * @param reminderEnabled 是否启用提醒
     */
    fun createDayCountdown(
        name: String,
        targetDate: String,
        description: String? = null,
        icon: String = "⏰",
        color: String = "#FF5722",
        reminderTime: String? = null,
        reminderEnabled: Boolean = false,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val configId = checkInRepository.createDayCountdown(
                name = name,
                targetDate = targetDate,
                description = description,
                icon = icon,
                color = color,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )
            onSuccess(configId)
        }
    }

    /**
     * 创建打卡倒计时
     * @param name 倒计时名称
     * @param countdownTarget 倒计时目标次数
     * @param tag 标签
     * @param description 描述
     * @param icon 图标
     * @param color 颜色
     * @param reminderTime 提醒时间
     * @param reminderEnabled 是否启用提醒
     */
    fun createCheckInCountdown(
        name: String,
        countdownTarget: Int,
        tag: String? = null,
        description: String? = null,
        icon: String = "📅",
        color: String = "#2196F3",
        reminderTime: String? = null,
        reminderEnabled: Boolean = false,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val configId = checkInRepository.createCheckInCountdown(
                name = name,
                countdownTarget = countdownTarget,
                tag = tag,
                description = description,
                icon = icon,
                color = color,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )
            onSuccess(configId)
        }
    }

    /**
     * 打卡倒计时打卡
     */
    fun checkInCountdown(configId: Long, tag: String? = null, note: String? = null) {
        viewModelScope.launch {
            checkInRepository.checkInCountdown(
                configId = configId,
                tag = tag,
                note = note
            )
        }
    }

    /**
     * 计算天数倒计时的剩余天数
     */
    fun calculateDaysRemaining(targetDate: String): Int {
        return checkInRepository.calculateDaysRemaining(targetDate)
    }

    /**
     * 获取打卡倒计时的剩余次数
     */
    fun getCheckInCountdownRemaining(config: UnifiedCheckInConfig): Int {
        return checkInRepository.getCheckInCountdownRemaining(config)
    }

    /**
     * 获取倒计时进度百分比
     */
    fun getCountdownProgress(config: UnifiedCheckInConfig): Float {
        return checkInRepository.getCountdownProgress(config)
    }
    
    /**
     * 创建正向打卡配置
     * @param name 打卡名称
     * @param recurrenceType 重复类型
     * @param description 描述
     * @param icon 图标
     * @param color 颜色
     * @param reminderTime 提醒时间
     * @param reminderEnabled 是否启用提醒
     * @return 创建的配置ID
     */
    fun createPositiveCheckIn(
        name: String,
        recurrenceType: com.love.diary.data.model.RecurrenceType,
        description: String? = null,
        icon: String = "✅",
        color: String = "#4CAF50",
        reminderTime: String? = null,
        reminderEnabled: Boolean = false,
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val configId = checkInRepository.createPositiveCheckIn(
                name = name,
                recurrenceType = recurrenceType,
                description = description,
                icon = icon,
                color = color,
                reminderTime = reminderTime,
                reminderEnabled = reminderEnabled
            )
            onSuccess(configId)
        }
    }
    
    /**
     * 正向打卡
     */
    fun checkInPositive(configId: Long) {
        viewModelScope.launch {
            checkInRepository.checkInPositive(configId)
        }
    }
}