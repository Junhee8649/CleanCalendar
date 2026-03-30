package com.github.junhee8649.cleancalendar.history

import androidx.lifecycle.viewModelScope
import com.github.junhee8649.cleancalendar.data.WorkLog
import com.github.junhee8649.cleancalendar.data.WorkLogRepository
import com.github.junhee8649.cleancalendar.util.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val workLogRepository: WorkLogRepository
) : BaseViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val workLogs: List<WorkLog> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        loadWorkLogs()
    }

    fun loadWorkLogs() {
        viewModelScope.launch(crashPreventionHandler) {
            val isFirstLoad = _uiState.value.workLogs.isEmpty()
            if (isFirstLoad) {
                _uiState.update { it.copy(isLoading = true) }
            }
            try {
                val workLogs = workLogRepository.getWorkLogs()
                _uiState.update { it.copy(workLogs = workLogs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _userMessage.value = e.message
            }
        }
    }

    fun deleteWorkLog(id: String) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                workLogRepository.deleteWorkLog(id)
                loadWorkLogs()
            } catch (e: Exception) {
                _userMessage.value = e.message
            }
        }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }
}
