// presentation/viewmodel/SettingsViewModel.kt
package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: AppRepository
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
                val updated = it.copy(showMoodTip = show)
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(showMoodTip = show) }
            }
        }
    }

    fun toggleStreak(show: Boolean) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                val updated = it.copy(showStreak = show)
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(showStreak = show) }
            }
        }
    }

    fun toggleAnniversary(show: Boolean) {
        viewModelScope.launch {
            val config = repository.getAppConfig()
            config?.let {
                val updated = it.copy(showAnniversary = show)
                repository.updateAppConfig(updated)
                _uiState.update { state -> state.copy(showAnniversary = show) }
            }
        }
    }

    fun exportData() {
        // TODO: 实现导出功能
    }

    fun importData() {
        // TODO: 实现导入功能
    }

    fun resetData() {
        // TODO: 实现重置功能
    }
}