package com.github.junhee8649.cleancalendar.history

import android.net.Uri
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
import java.time.LocalDate
import java.util.UUID

class WorkLogFormViewModel(
    private val workLogRepository: WorkLogRepository,
    private val schoolRepository: SchoolRepository
) : BaseViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val schools: List<School> = emptyList(),
        val selectedSchoolId: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        val selectedCategories: Set<String> = emptySet(),
        val issuesText: String = "",
        val selectedImageUris: List<Uri> = emptyList(),
        val isUploading: Boolean = false,
        val isSaved: Boolean = false,
        val userMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSchools()
    }

    private fun loadSchools() {
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

    fun selectSchool(schoolId: String) {
        _uiState.update { it.copy(selectedSchoolId = schoolId) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun toggleCategory(category: String) {
        _uiState.update { state ->
            val updated = if (category in state.selectedCategories)
                state.selectedCategories - category
            else
                state.selectedCategories + category
            state.copy(selectedCategories = updated)
        }
    }

    fun updateIssuesText(text: String) {
        _uiState.update { it.copy(issuesText = text) }
    }

    fun addImageUris(uris: List<Uri>) {
        _uiState.update { state ->
            val combined = (state.selectedImageUris + uris).take(5)
            state.copy(selectedImageUris = combined)
        }
    }

    fun removeImageUri(uri: Uri) {
        _uiState.update { state ->
            state.copy(selectedImageUris = state.selectedImageUris - uri)
        }
    }

    // imageBytesList: Screen에서 Context로 압축한 ByteArray 목록
    fun saveWorkLog(imageBytesList: List<ByteArray>) {
        val state = _uiState.value
        val schoolId = state.selectedSchoolId ?: run {
            _uiState.update { it.copy(userMessage = "학교를 선택해주세요.") }
            return
        }

        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isUploading = true) }
            try {
                val imageUrls = imageBytesList.mapIndexed { i, bytes ->
                    val fileName = "${UUID.randomUUID()}_$i.webp"
                    workLogRepository.uploadImage(bytes, fileName)
                }

                val workLog = WorkLog(
                    id = UUID.randomUUID().toString(),
                    schoolId = schoolId,
                    schoolName = state.schools.find { it.id == schoolId }?.name ?: "",
                    date = state.selectedDate,
                    taskCategories = state.selectedCategories.toList(),
                    issuesText = state.issuesText.trim(),
                    imageUrls = imageUrls
                )

                workLogRepository.addWorkLog(workLog)
                _uiState.update { it.copy(isUploading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, userMessage = e.message) }
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
