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
    
    fun checkInLoveDiary(moodType: MoodType) {
        val config = _uiState.value.currentCheckInConfig
        if (config != null && config.type == CheckInType.LOVE_DIARY) {
            viewModelScope.launch {
                checkInRepository.checkInLoveDiary(config.name, moodType)
            }
        }
    }
    
    fun checkInHabit(habitId: Long, tag: String? = null) {
        val config = _uiState.value.currentCheckInConfig
        if (config != null && config.type == CheckInType.HABIT) {
            viewModelScope.launch {
                checkInRepository.checkInHabit(config.name, habitId, tag)
            }
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
}