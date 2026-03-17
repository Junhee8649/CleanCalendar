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
        val selectedDate: LocalDate = LocalDate.now(),
        val tasksInMonth: List<MaintenanceTask> = emptyList(),
        // completedDate -> task ids (for dot rendering)
        val completedDates: Set<LocalDate> = emptySet(),
        val schoolNames: Map<String, String> = emptyMap(),
        val userMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isLoading = true, currentYearMonth = yearMonth) }
            try {
                val tasks = taskRepository.getTasksByMonth(yearMonth.year, yearMonth.monthValue)
                val schools = schoolRepository.getSchools()
                val schoolNames = schools.associate { it.id to it.name }
                val sortedTasks = tasks.sortedBy { schoolNames[it.schoolId] ?: "" }
                val completedDates = sortedTasks.mapNotNull { it.completedDate }.toSet()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasksInMonth = sortedTasks,
                        completedDates = completedDates,
                        schoolNames = schoolNames
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = e.message) }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun previousMonth() {
        loadMonth(_uiState.value.currentYearMonth.minusMonths(1))
    }

    fun nextMonth() {
        loadMonth(_uiState.value.currentYearMonth.plusMonths(1))
    }

    fun toggleTaskCompleted(task: MaintenanceTask) {
        viewModelScope.launch(crashPreventionHandler) {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedDate = if (!task.isCompleted) LocalDate.now() else null
            )
            // Optimistic update: 재조회 없이 현재 list에서 해당 item만 교체 (위치 유지)
            _uiState.update { state ->
                val updatedTasks = state.tasksInMonth.map { if (it.id == updated.id) updated else it }
                val completedDates = updatedTasks.mapNotNull { it.completedDate }.toSet()
                state.copy(tasksInMonth = updatedTasks, completedDates = completedDates)
            }
            try {
                taskRepository.updateTask(updated)
            } catch (e: Exception) {
                // 실패 시 rollback (재조회)
                val yearMonth = _uiState.value.currentYearMonth
                val tasks = taskRepository.getTasksByMonth(yearMonth.year, yearMonth.monthValue)
                val completedDates = tasks.mapNotNull { it.completedDate }.toSet()
                _uiState.update { it.copy(tasksInMonth = tasks, completedDates = completedDates, userMessage = e.message) }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
