package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import com.love.diary.data.model.HabitType
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val selectedHabit: Habit? = null,
    val habitRecords: List<HabitRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCreatingHabit: Boolean = false,
    val newHabitName: String = "",
    val newHabitDescription: String = "",
    val newHabitButtonLabel: String = "打卡",
    val newHabitType: HabitType = HabitType.POSITIVE,
    val newHabitTargetDate: String? = null,
    val newHabitTags: String = ""
)

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()
    
    init {
        loadAllHabits()
    }
    
    private fun loadAllHabits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getAllHabits().collect { habits ->
                _uiState.update { state ->
                    state.copy(
                        habits = habits,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun loadHabitRecords(habitId: Long) {
        viewModelScope.launch {
            repository.getHabitRecordsFlow(habitId).collect { records ->
                _uiState.update { state ->
                    state.copy(
                        habitRecords = records,
                        selectedHabit = state.selectedHabit
                    )
                }
            }
        }
    }
    
    fun selectHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId)
            _uiState.update { state ->
                state.copy(selectedHabit = habit)
            }
            
            if (habit != null) {
                loadHabitRecords(habitId)
            }
        }
    }
    
    fun unselectHabit() {
        _uiState.update { state ->
            state.copy(selectedHabit = null, habitRecords = emptyList())
        }
    }
    
    // 打卡操作
    fun checkInHabit(habitId: Long, note: String? = null) {
        viewModelScope.launch {
            val success = repository.checkInHabit(habitId, note)
            if (success) {
                // 重新加载习惯列表以更新计数
                loadAllHabits()
            }
        }
    }
    
    // 带标签的打卡操作
    fun checkInHabitWithTag(habitId: Long, tag: String?) {
        viewModelScope.launch {
            val success = repository.checkInHabitWithTag(habitId, tag)
            if (success) {
                // 重新加载习惯列表以更新计数
                loadAllHabits()
            }
        }
    }
    
    // 创建新习惯
    fun createHabit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val newHabit = Habit(
                name = _uiState.value.newHabitName,
                description = _uiState.value.newHabitDescription.ifEmpty { null },
                buttonLabel = _uiState.value.newHabitButtonLabel,
                type = _uiState.value.newHabitType,
                targetDate = if (_uiState.value.newHabitType == HabitType.COUNTDOWN) _uiState.value.newHabitTargetDate else null,
                startDate = LocalDate.now().toString(),
                tags = _uiState.value.newHabitTags
            )
            
            try {
                repository.createHabit(newHabit)
                resetNewHabitForm()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    ) 
                }
            }
        }
    }
    
    // 更新新习惯表单字段
    fun updateNewHabitName(name: String) {
        _uiState.update { it.copy(newHabitName = name) }
    }
    
    fun updateNewHabitDescription(description: String) {
        _uiState.update { it.copy(newHabitDescription = description) }
    }
    
    fun updateNewHabitButtonLabel(label: String) {
        _uiState.update { it.copy(newHabitButtonLabel = label) }
    }
    
    fun updateNewHabitType(type: HabitType) {
        _uiState.update { it.copy(newHabitType = type) }
    }
    
    fun updateNewHabitTargetDate(date: String?) {
        _uiState.update { it.copy(newHabitTargetDate = date) }
    }
    
    fun updateNewHabitTags(tags: String) {
        _uiState.update { it.copy(newHabitTags = tags) }
    }
    
    private fun resetNewHabitForm() {
        _uiState.update { it.copy(
            newHabitName = "",
            newHabitDescription = "",
            newHabitButtonLabel = "打卡",
            newHabitType = HabitType.POSITIVE,
            newHabitTargetDate = null,
            newHabitTags = ""
        ) }
    }
    
    // 删除习惯
    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}