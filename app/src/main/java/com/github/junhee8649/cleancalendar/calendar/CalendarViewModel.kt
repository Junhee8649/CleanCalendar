package com.github.junhee8649.cleancalendar.calendar

import androidx.lifecycle.viewModelScope
import com.github.junhee8649.cleancalendar.data.MaintenanceTask
import com.github.junhee8649.cleancalendar.data.MaintenanceTaskRepository
import com.github.junhee8649.cleancalendar.data.SchoolRepository
import com.github.junhee8649.cleancalendar.util.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val taskRepository: MaintenanceTaskRepository,
    private val schoolRepository: SchoolRepository
) : BaseViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val currentYearMonth: YearMonth = YearMonth.now(),
        val selectedDate: LocalDate? = null,
        val tasksInMonth: List<MaintenanceTask> = emptyList(),
        val schoolNames: Map<String, String> = emptyMap(),
        val userMessage: String? = null,
        val showTaskPicker: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    private fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isLoading = true, currentYearMonth = yearMonth) }
            try {
                val tasks = taskRepository.getTasksByMonth(yearMonth.year, yearMonth.monthValue)
                val schools = schoolRepository.getSchools()
                val schoolNames = schools.associate { it.id to it.name }
                val sortedTasks = tasks.sortedBy { schoolNames[it.schoolId] ?: "" }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasksInMonth = sortedTasks,
                        schoolNames = schoolNames
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = e.message) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { state ->
            val newDate = if (state.selectedDate == date) null else date
            state.copy(selectedDate = newDate)
        }
    }

    fun previousMonth() {
        loadMonth(_uiState.value.currentYearMonth.minusMonths(1))
    }

    fun nextMonth() {
        loadMonth(_uiState.value.currentYearMonth.plusMonths(1))
    }

    fun showTaskPicker() {
        _uiState.update { it.copy(showTaskPicker = true) }
    }

    fun hideTaskPicker() {
        _uiState.update { it.copy(showTaskPicker = false) }
    }

    // 미배정 task를 선택한 날짜에 배정 (UPDATE)
    fun addTaskForDate(taskId: String) {
        val date = _uiState.value.selectedDate ?: return
        val task = _uiState.value.tasksInMonth.firstOrNull { it.id == taskId } ?: return
        val updated = task.copy(scheduledDate = date)
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { state ->
                state.copy(tasksInMonth = state.tasksInMonth.map { if (it.id == taskId) updated else it })
            }
            try {
                taskRepository.updateTask(updated)
            } catch (e: Exception) {
                loadMonth(_uiState.value.currentYearMonth)
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    // scheduledDate = null 로 되돌림 (UPDATE)
    fun removeTaskFromDate(taskId: String) {
        val task = _uiState.value.tasksInMonth.firstOrNull { it.id == taskId } ?: return
        val updated = task.copy(scheduledDate = null)
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { state ->
                state.copy(tasksInMonth = state.tasksInMonth.map { if (it.id == taskId) updated else it })
            }
            try {
                taskRepository.updateTask(updated)
            } catch (e: Exception) {
                loadMonth(_uiState.value.currentYearMonth)
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
