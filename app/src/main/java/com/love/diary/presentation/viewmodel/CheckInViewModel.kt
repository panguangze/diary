package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.model.CheckIn
import com.love.diary.data.model.CheckInConfig
import com.love.diary.data.model.CheckInType
import com.love.diary.data.model.MoodType
import com.love.diary.data.repository.CheckInRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckInUiState(
    val currentCheckInConfig: CheckInConfig? = null,
    val checkInRecords: List<CheckIn> = emptyList(),
    val allCheckInConfigs: List<CheckInConfig> = emptyList(),
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
    
    private fun loadCheckInTypes() {
        viewModelScope.launch {
            checkInRepository.getUniqueCheckInTypes().collect { types ->
                _uiState.update { state ->
                    state.copy(
                        checkInTypes = types
                    )
                }
            }
        }
    }
    
    fun selectCheckInConfig(config: CheckInConfig) {
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
        note: String? = null,
        attachmentUri: String? = null,
        duration: Int? = null,
        rating: Int? = null,
        count: Int = 1
    ) {
        viewModelScope.launch {
            checkInRepository.checkIn(
                name = name,
                type = type,
                moodType = moodType,
                tag = tag,
                note = note,
                attachmentUri = attachmentUri,
                duration = duration,
                rating = rating,
                count = count
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
    
    fun checkInMilestone(
        name: String,
        note: String? = null,
        attachmentUri: String? = null,
        rating: Int? = null
    ) {
        viewModelScope.launch {
            checkInRepository.checkInMilestone(
                name = name,
                note = note,
                attachmentUri = attachmentUri,
                rating = rating
            )
        }
    }
    
    fun checkInDailyTask(
        name: String,
        note: String? = null,
        duration: Int? = null,
        isCompleted: Boolean = true
    ) {
        viewModelScope.launch {
            checkInRepository.checkInDailyTask(
                name = name,
                note = note,
                duration = duration,
                isCompleted = isCompleted
            )
        }
    }
    
    fun createCheckInConfig(config: CheckInConfig) {
        viewModelScope.launch {
            checkInRepository.saveCheckInConfig(config)
        }
    }
    
    fun updateCheckInConfig(config: CheckInConfig) {
        viewModelScope.launch {
            checkInRepository.updateCheckInConfig(config)
        }
    }
    
    fun deleteCheckInConfig(id: Long) {
        viewModelScope.launch {
            checkInRepository.deleteCheckInConfig(id)
        }
    }
    
    // 获取指定日期范围内的打卡记录
    fun loadCheckInsBetweenDates(startDate: String, endDate: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val records = checkInRepository.getCheckInsBetweenDates(startDate, endDate).value
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
            val records = checkInRepository.getCheckInsByTypeAndDateRange(type, startDate, endDate).value
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
}