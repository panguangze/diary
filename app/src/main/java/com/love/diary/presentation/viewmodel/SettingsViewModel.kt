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

data class SettingsUiState(
    val startDate: String? = null,
    val coupleName: String? = null,
    val partnerNickname: String? = null,
    val showMoodTip: Boolean = true,
    val showStreak: Boolean = true,
    val showAnniversary: Boolean = true,
    val isLoading: Boolean = true
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
                    // 导出成功，可以显示通知或提示
                    // 这里可以添加成功提示逻辑
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun exportDataToUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val result = backupManager.exportDataToUri(uri)
                if (result.isSuccess) {
                    // 导出成功，可以显示通知或提示
                    // 这里可以添加成功提示逻辑
                }
            } catch (e: Exception) {
                // 处理错误
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
                    // 通知其他组件配置已更改
                }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            // 清空所有数据
            repository.clearAllMoodRecords()
            repository.deleteAppConfig()
        }
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
}