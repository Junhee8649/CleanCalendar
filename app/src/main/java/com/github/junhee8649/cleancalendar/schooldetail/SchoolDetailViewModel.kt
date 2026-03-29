package com.github.junhee8649.cleancalendar.schooldetail

import androidx.lifecycle.viewModelScope
import com.github.junhee8649.cleancalendar.data.MaintenanceTask
import com.github.junhee8649.cleancalendar.data.MaintenanceTaskRepository
import com.github.junhee8649.cleancalendar.data.School
import com.github.junhee8649.cleancalendar.data.SchoolRepository
import com.github.junhee8649.cleancalendar.util.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SchoolDetailViewModel(
    private val schoolRepository: SchoolRepository,
    private val taskRepository: MaintenanceTaskRepository
) : BaseViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val school: School? = null,
        val tasks: List<MaintenanceTask> = emptyList(),
        val userMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData(schoolId: String) {
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val school = schoolRepository.getSchool(schoolId)
                val tasks = taskRepository.getTasksBySchool(schoolId)
                _uiState.update { it.copy(isLoading = false, school = school, tasks = tasks) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = e.message) }
            }
        }
    }

    fun updateSchool(school: School) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                schoolRepository.updateSchool(school)
                _uiState.update { it.copy(school = school) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun addTask(schoolId: String, year: Int, month: Int, taskDescription: String) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                val task = MaintenanceTask(
                    id = UUID.randomUUID().toString(),
                    schoolId = schoolId,
                    month = month,
                    year = year,
                    taskDescription = taskDescription,
                    isCompleted = false,
                    completedDate = null,
                    notes = "",
                    scheduledDate = null
                )
                taskRepository.addTask(task)
                val tasks = taskRepository.getTasksBySchool(schoolId)
                _uiState.update { it.copy(tasks = tasks) }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun toggleTaskCompleted(task: MaintenanceTask) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                val updated = task.copy(
                    isCompleted = !task.isCompleted,
                    completedDate = if (!task.isCompleted) java.time.LocalDate.now() else null
                )
                taskRepository.updateTask(updated)
                _uiState.update { state ->
                    state.copy(tasks = state.tasks.map { if (it.id == task.id) updated else it })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                taskRepository.deleteTask(id)
                _uiState.update { state ->
                    state.copy(tasks = state.tasks.filter { it.id != id })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
