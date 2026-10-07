package com.example.ui.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ExamTrackApplication
import com.example.data.model.Subject
import com.example.notification.NotificationHelper
import com.example.ui.theme.PresetColorHexes
import com.example.util.DateUtils
import com.example.widget.ExamTrackWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddEditSubjectUiState(
    val subjectId: String? = null,
    val name: String = "",
    val examDate: Long = 0L,
    val selectedColor: String = PresetColorHexes.first(),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
) {
    val isSaveEnabled: Boolean
        get() = name.trim().isNotEmpty() && examDate > 0L
}

class AddEditSubjectViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = (application as ExamTrackApplication).repository
    private val _uiState = MutableStateFlow(AddEditSubjectUiState())
    val uiState: StateFlow<AddEditSubjectUiState> = _uiState.asStateFlow()

    fun initialize(subjectId: String?) {
        if (subjectId.isNullOrEmpty()) {
            // New Subject: default date to tomorrow
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7) // convenient default: 1 week ahead
            }.timeInMillis

            _uiState.update {
                it.copy(
                    subjectId = null,
                    isEditMode = false,
                    examDate = DateUtils.startOfDay(tomorrow),
                    selectedColor = PresetColorHexes.first()
                )
            }
        } else {
            // Edit existing
            viewModelScope.launch {
                val subject = repository.getSubjectById(subjectId)
                if (subject != null) {
                    _uiState.update {
                        it.copy(
                            subjectId = subject.id,
                            name = subject.name,
                            examDate = subject.examDate,
                            selectedColor = subject.colorTag,
                            isEditMode = true
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, errorMessage = null) }
    }

    fun onExamDateChange(newDate: Long) {
        _uiState.update { it.copy(examDate = DateUtils.startOfDay(newDate), errorMessage = null) }
    }

    fun onColorSelect(hexColor: String) {
        _uiState.update { it.copy(selectedColor = hexColor) }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmation = show) }
    }

    fun saveSubject() {
        val state = _uiState.value
        val trimmedName = state.name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Subject name cannot be empty") }
            return
        }
        if (state.examDate <= 0L) {
            _uiState.update { it.copy(errorMessage = "Please choose an exam date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val context = getApplication<Application>()

            if (state.isEditMode && state.subjectId != null) {
                val updatedSubject = Subject(
                    id = state.subjectId,
                    name = trimmedName,
                    examDate = state.examDate,
                    colorTag = state.selectedColor
                )
                repository.updateSubject(updatedSubject)

                // Reschedule notifications
                NotificationHelper.cancelExamReminders(context, updatedSubject.id)
                NotificationHelper.scheduleExamReminders(context, updatedSubject)
            } else {
                val newSubject = Subject(
                    name = trimmedName,
                    examDate = state.examDate,
                    colorTag = state.selectedColor
                )
                repository.insertSubject(newSubject)

                // Schedule notifications
                NotificationHelper.scheduleExamReminders(context, newSubject)
            }

            // Update Widgets
            ExamTrackWidgetProvider.updateAllWidgets(context)

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun deleteSubject() {
        val state = _uiState.value
        val subjectId = state.subjectId ?: return

        viewModelScope.launch {
            val context = getApplication<Application>()
            NotificationHelper.cancelExamReminders(context, subjectId)
            repository.deleteSubjectById(subjectId)
            ExamTrackWidgetProvider.updateAllWidgets(context)

            _uiState.update {
                it.copy(
                    showDeleteConfirmation = false,
                    isDeleted = true
                )
            }
        }
    }
}
