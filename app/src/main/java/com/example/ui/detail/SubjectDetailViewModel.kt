package com.example.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ExamTrackApplication
import com.example.data.model.SubjectWithTopics
import com.example.data.model.Topic
import com.example.notification.NotificationHelper
import com.example.widget.ExamTrackWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectDetailUiState(
    val subjectWithTopics: SubjectWithTopics? = null,
    val newTopicTitle: String = "",
    val showDeleteDialog: Boolean = false,
    val isSubjectDeleted: Boolean = false,
    val isLoading: Boolean = true
)

class SubjectDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = (application as ExamTrackApplication).repository
    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()

    private var currentSubjectId: String = ""

    fun initialize(subjectId: String) {
        currentSubjectId = subjectId
        viewModelScope.launch {
            repository.getSubjectWithTopics(subjectId).collect { data ->
                _uiState.update {
                    it.copy(
                        subjectWithTopics = data,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNewTopicTitleChange(title: String) {
        _uiState.update { it.copy(newTopicTitle = title) }
    }

    fun addTopic() {
        val title = _uiState.value.newTopicTitle.trim()
        if (title.isEmpty() || currentSubjectId.isEmpty()) return

        val currentTopics = _uiState.value.subjectWithTopics?.topics ?: emptyList()
        val newOrder = currentTopics.size

        viewModelScope.launch {
            val newTopic = Topic(
                subjectId = currentSubjectId,
                title = title,
                isDone = false,
                order = newOrder
            )
            repository.insertTopic(newTopic)
            _uiState.update { it.copy(newTopicTitle = "") }
            ExamTrackWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun toggleTopicDone(topic: Topic) {
        viewModelScope.launch {
            val updated = topic.copy(isDone = !topic.isDone)
            repository.updateTopic(updated)
            ExamTrackWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun deleteTopic(topicId: String) {
        viewModelScope.launch {
            repository.deleteTopicById(topicId)
            ExamTrackWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun moveTopicUp(topic: Topic) {
        val topics = _uiState.value.subjectWithTopics?.topics ?: return
        val index = topics.indexOfFirst { it.id == topic.id }
        if (index > 0) {
            val mutableList = topics.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index - 1]
            mutableList[index - 1] = temp
            viewModelScope.launch {
                repository.updateTopicsOrder(mutableList)
            }
        }
    }

    fun moveTopicDown(topic: Topic) {
        val topics = _uiState.value.subjectWithTopics?.topics ?: return
        val index = topics.indexOfFirst { it.id == topic.id }
        if (index in 0 until topics.size - 1) {
            val mutableList = topics.toMutableList()
            val temp = mutableList[index]
            mutableList[index] = mutableList[index + 1]
            mutableList[index + 1] = temp
            viewModelScope.launch {
                repository.updateTopicsOrder(mutableList)
            }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun deleteSubject() {
        if (currentSubjectId.isEmpty()) return
        viewModelScope.launch {
            val context = getApplication<Application>()
            NotificationHelper.cancelExamReminders(context, currentSubjectId)
            repository.deleteSubjectById(currentSubjectId)
            ExamTrackWidgetProvider.updateAllWidgets(context)
            _uiState.update { it.copy(showDeleteDialog = false, isSubjectDeleted = true) }
        }
    }
}
