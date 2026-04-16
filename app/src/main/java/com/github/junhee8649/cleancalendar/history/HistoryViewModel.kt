package com.github.junhee8649.cleancalendar.history

import androidx.lifecycle.viewModelScope
import com.github.junhee8649.cleancalendar.data.School
import com.github.junhee8649.cleancalendar.data.SchoolRepository
import com.github.junhee8649.cleancalendar.data.WorkLog
import com.github.junhee8649.cleancalendar.data.WorkLogRepository
import com.github.junhee8649.cleancalendar.util.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val workLogRepository: WorkLogRepository,
    private val schoolRepository: SchoolRepository
) : BaseViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val schools: List<School> = emptyList(),
        val workLogs: List<WorkLog> = emptyList(),
        val selectedSchoolId: String? = null,
        val searchQuery: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isLoading = it.schools.isEmpty()) }
            try {
                val schools = schoolRepository.getSchools()
                val workLogs = workLogRepository.getWorkLogs()
                _uiState.update { it.copy(schools = schools, workLogs = workLogs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _userMessage.value = e.message
            }
        }
    }

    fun loadWorkLogs() {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                val workLogs = workLogRepository.getWorkLogs()
                _uiState.update { it.copy(workLogs = workLogs) }
            } catch (e: Exception) {
                _userMessage.value = e.message
            }
        }
    }

    fun selectSchool(schoolId: String) {
        _uiState.update { it.copy(selectedSchoolId = schoolId) }
    }

    fun clearSchoolSelection() {
        _uiState.update { it.copy(selectedSchoolId = null) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
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
