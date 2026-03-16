package com.github.junhee8649.cleancalendar.schools

import androidx.lifecycle.viewModelScope
import com.github.junhee8649.cleancalendar.data.School
import com.github.junhee8649.cleancalendar.data.SchoolRepository
import com.github.junhee8649.cleancalendar.util.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SchoolsViewModel(
    private val schoolRepository: SchoolRepository
) : BaseViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val schools: List<School> = emptyList(),
        val userMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSchools()
    }

    fun loadSchools() {
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val schools = schoolRepository.getSchools()
                _uiState.update { it.copy(schools = schools, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, userMessage = e.message) }
            }
        }
    }

    fun addSchool(school: School) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                schoolRepository.addSchool(school)
                loadSchools()
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun deleteSchool(id: String) {
        viewModelScope.launch(crashPreventionHandler) {
            try {
                schoolRepository.deleteSchool(id)
                loadSchools()
            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = e.message) }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
