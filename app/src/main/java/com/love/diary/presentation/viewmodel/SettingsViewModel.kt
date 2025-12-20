// presentation/viewmodel/SettingsViewModel.kt
package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.backup.DataBackupManager
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for Settings screen
 */
data class SettingsUiState(
    val startDate: String? = null,
    val coupleName: String? = null,
    val partnerNickname: String? = null,
    val showMoodTip: Boolean = true,
    val showStreak: Boolean = true,
    val showAnniversary: Boolean = true,
    /** Dark mode: null = follow system, true = dark, false = light */
    val darkMode: Boolean? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: Int = 540, // Time in minutes from midnight (default 9:00 AM)
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AppRepository,
    private val backupManager: DataBackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                _uiState.update { state ->
                    state.copy(
                        startDate = it.startDate,
                        coupleName = it.coupleName,
                        partnerNickname = it.partnerNickname,
                        showMoodTip = it.showMoodTip,
                        showStreak = it.showStreak,
                        showAnniversary = it.showAnniversary,
                        darkMode = it.darkMode,
                        reminderEnabled = it.reminderEnabled,
                        reminderTime = it.reminderTime,
                        isLoading = false
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleMoodTip(show: Boolean) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                val updated = it.copy(
                    showMoodTip = show,
                    startTimeMinutes = it.startTimeMinutes, // 保留现有的startTimeMinutes
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(showMoodTip = show) }
            }
        }
    }

    fun toggleStreak(show: Boolean) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                val updated = it.copy(
                    showStreak = show,
                    startTimeMinutes = it.startTimeMinutes, // 保留现有的startTimeMinutes
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(showStreak = show) }
            }
        }
    }

    fun toggleAnniversary(show: Boolean) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                val updated = it.copy(
                    showAnniversary = show,
                    startTimeMinutes = it.startTimeMinutes, // 保留现有的startTimeMinutes
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(showAnniversary = show) }
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                val result = backupManager.exportData()
                if (result.isSuccess) {
                    _uiState.update { it.copy(successMessage = "数据导出成功") }
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.update { it.copy(errorMessage = "导出失败: ${error?.message}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "导出失败: ${e.message}") }
            }
        }
    }

    fun exportDataToUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val result = backupManager.exportDataToUri(uri)
                if (result.isSuccess) {
                    _uiState.update { it.copy(successMessage = "数据导出成功") }
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.update { it.copy(errorMessage = "导出失败: ${error?.message}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "导出失败: ${e.message}") }
            }
        }
    }

    fun importData() {
        // 导入数据需要从UI层获取Uri，这里只提供逻辑框架
        // 实际的导入会在Activity中处理
    }

    fun importDataFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val result = backupManager.importData(uri)
                if (result.isSuccess) {
                    // 导入成功，刷新UI状态
                    loadSettings()
                    _uiState.update { it.copy(successMessage = "数据导入成功") }
                } else {
                    val error = result.exceptionOrNull()
                    _uiState.update { it.copy(errorMessage = "导入失败: ${error?.message}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "导入失败: ${e.message}") }
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            try {
                // 清空所有数据
                repository.clearAllMoodRecords()
                repository.deleteAppConfig()
                _uiState.update { it.copy(successMessage = "数据已重置") }
                loadSettings()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "重置失败: ${e.message}") }
            }
        }
    }
    
    /**
     * Clear error or success message
     */
    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
    
    fun updateStartDate(date: String) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            val updated = if (config != null) {
                config.copy(
                    startDate = date,
                    startTimeMinutes = config.startTimeMinutes, // 保留现有的startTimeMinutes
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                AppConfigEntity(
                    id = 1,
                    startDate = date,
                    startTimeMinutes = 0, // 默认值
                    coupleName = null,
                    partnerNickname = null,
                    showMoodTip = true,
                    showStreak = true,
                    showAnniversary = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            repository.updateAppConfig(updated)
            _uiState.update { state -> state.copy(startDate = date) }
        }
    }
    
    fun updateCoupleName(name: String) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            val oldCoupleName = config?.coupleName
            
            val updated = if (config != null) {
                config.copy(
                    coupleName = name,
                    startTimeMinutes = config.startTimeMinutes, // 保留现有的startTimeMinutes
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                AppConfigEntity(
                    id = 1,
                    startDate = "", // startDate is non-null, so provide empty string as default
                    startTimeMinutes = 0, // 默认值
                    coupleName = name,
                    partnerNickname = null,
                    showMoodTip = true,
                    showStreak = true,
                    showAnniversary = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            
            // 确定旧的默认打卡名称（按优先级查找）
            val oldDefaultName = oldCoupleName ?: "异地恋日记"
            
            // 更新默认打卡事项的名称（Legacy Habit系统）
            val defaultHabit = repository.getHabitByName(oldDefaultName)
            if (defaultHabit != null && name.isNotBlank()) {
                val updatedHabit = defaultHabit.copy(
                    name = name,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateHabit(updatedHabit)
            }
            
            // 同时更新UnifiedCheckInConfig的名称（新统一打卡系统）
            val defaultConfig = repository.getCheckInConfigByName(oldDefaultName)
            if (defaultConfig != null && name.isNotBlank()) {
                val updatedConfig = defaultConfig.copy(
                    name = name,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateCheckInConfig(updatedConfig)
                
                // 更新现有UnifiedCheckIn记录的名称，保持数据一致性
                // 这样历史记录和统计页面可以继续使用新名称访问数据
                repository.updateCheckInRecordsName(oldDefaultName, name)
            }
            
            repository.updateAppConfig(updated)
            _uiState.update { state -> state.copy(coupleName = name) }
        }
    }
    
    fun updatePartnerNickname(nickname: String) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            val updated = if (config != null) {
                config.copy(
                    partnerNickname = nickname,
                    startTimeMinutes = config.startTimeMinutes, // 保留现有的startTimeMinutes
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                AppConfigEntity(
                    id = 1,
                    startDate = "", // startDate is non-null, so provide empty string as default
                    startTimeMinutes = 0, // 默认值
                    coupleName = null,
                    partnerNickname = nickname,
                    showMoodTip = true,
                    showStreak = true,
                    showAnniversary = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            repository.updateAppConfig(updated)
            _uiState.update { state -> state.copy(partnerNickname = nickname) }
        }
    }
    
    /**
     * Toggle dark mode setting
     * @param darkMode null = follow system, true = dark theme, false = light theme
     */
    fun setDarkMode(darkMode: Boolean?) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                val updated = it.copy(
                    darkMode = darkMode,
                    startTimeMinutes = it.startTimeMinutes,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(darkMode = darkMode) }
            }
        }
    }

    /**
     * Toggle reminder enabled status
     */
    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateReminderEnabled(enabled)
            _uiState.update { state -> state.copy(reminderEnabled = enabled) }
        }
    }

    /**
     * Update reminder time
     * @param timeInMinutes Time in minutes from midnight (0-1439)
     */
    fun updateReminderTime(timeInMinutes: Int) {
        viewModelScope.launch {
            repository.updateReminderTime(timeInMinutes)
            _uiState.update { state -> state.copy(reminderTime = timeInMinutes) }
        }
    }
}