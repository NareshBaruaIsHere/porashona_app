package com.example.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ExamTrackApplication
import com.example.data.model.SubjectWithTopics
import com.example.util.CountdownStatus
import com.example.util.DateUtils
import com.example.widget.ExamTrackWidgetProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val subjects: List<SubjectWithTopics> = emptyList(),
    val isLoading: Boolean = false
) {
    val totalSubjects: Int get() = subjects.size
    val totalTopics: Int get() = subjects.sumOf { it.totalTopics }
    val completedTopics: Int get() = subjects.sumOf { it.completedTopics }
    val remainingTopics: Int get() = (totalTopics - completedTopics).coerceAtLeast(0)
    val overallProgress: Float
        get() = if (totalTopics > 0) completedTopics.toFloat() / totalTopics.toFloat() else 0f
    val overallPercentage: Int get() = (overallProgress * 100).toInt()

    val upcomingCount: Int
        get() = subjects.count {
            val status = DateUtils.getCountdownInfo(it.subject.examDate).status
            status == CountdownStatus.UPCOMING || status == CountdownStatus.URGENT || status == CountdownStatus.TODAY
        }

    val urgentCount: Int
        get() = subjects.count {
            DateUtils.getCountdownInfo(it.subject.examDate).status == CountdownStatus.URGENT
        }

    val todayCount: Int
        get() = subjects.count {
            DateUtils.getCountdownInfo(it.subject.examDate).status == CountdownStatus.TODAY
        }

    val passedCount: Int
        get() = subjects.count {
            DateUtils.getCountdownInfo(it.subject.examDate).status == CountdownStatus.PASSED
        }

    val nearestUpcomingExam: SubjectWithTopics?
        get() {
            val todayStart = DateUtils.startOfDay(System.currentTimeMillis())
            return subjects.firstOrNull { DateUtils.startOfDay(it.subject.examDate) >= todayStart }
        }
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ExamTrackApplication).repository

    val uiState: StateFlow<DashboardUiState> = repository.allSubjectsWithTopics
        .map { list ->
            // Sort by exam date ascending (soonest first)
            val sortedList = list.sortedBy { it.subject.examDate }
            // Update widget with latest data
            ExamTrackWidgetProvider.updateAllWidgets(getApplication())
            DashboardUiState(
                subjects = sortedList,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
