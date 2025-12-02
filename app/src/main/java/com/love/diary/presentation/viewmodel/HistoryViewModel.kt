// presentation/viewmodel/HistoryViewModel.kt
package com.love.diary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.love.diary.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _moodRecords = MutableStateFlow(emptyList<com.love.diary.data.database.entities.DailyMoodEntity>())
    val moodRecords: StateFlow<List<com.love.diary.data.database.entities.DailyMoodEntity>>
            = _moodRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getRecentMoods(limit = 100).collect { records ->
                _moodRecords.value = records
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadHistory()
    }
}