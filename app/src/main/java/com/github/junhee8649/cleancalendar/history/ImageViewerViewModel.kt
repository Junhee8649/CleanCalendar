package com.github.junhee8649.cleancalendar.history

import androidx.lifecycle.viewModelScope
import com.github.junhee8649.cleancalendar.data.WorkLogRepository
import com.github.junhee8649.cleancalendar.util.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageViewerViewModel(
    private val workLogId: String,
    private val workLogRepository: WorkLogRepository
) : BaseViewModel() {

    data class UiState(
        val imageUrls: List<String> = emptyList(),
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadImageUrls()
    }

    private fun loadImageUrls() {
        viewModelScope.launch(crashPreventionHandler) {
            _uiState.update { it.copy(isLoading = true) }
            val workLog = workLogRepository.getWorkLog(workLogId)
            _uiState.update {
                it.copy(
                    imageUrls = workLog?.imageUrls ?: emptyList(),
                    isLoading = false
                )
            }
        }
    }
}
