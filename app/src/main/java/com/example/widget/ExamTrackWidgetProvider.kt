package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.ExamTrackDatabase
import com.example.util.CountdownStatus
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExamTrackWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ExamTrackWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                updateWidgets(context, appWidgetManager, appWidgetIds)
            }
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = ExamTrackDatabase.getDatabase(context)
                val allSubjects = db.subjectDao().getAllSubjectsSync()

                // Filter for exams that are today or upcoming, sorted by date
                val todayStart = DateUtils.startOfDay(System.currentTimeMillis())
                val upcomingSubject = allSubjects.firstOrNull {
                    DateUtils.startOfDay(it.examDate) >= todayStart
                } ?: allSubjects.firstOrNull()

                val topics = if (upcomingSubject != null) {
                    db.topicDao().getTopicsForSubjectSync(upcomingSubject.id)
                } else {
                    emptyList()
                }

                val totalTopics = topics.size
                val doneTopics = topics.count { it.isDone }
                val progressPct = if (totalTopics > 0) ((doneTopics.toFloat() / totalTopics) * 100).toInt() else 0

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_exam_track)

                    // Pending intent to launch app
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        if (upcomingSubject != null) {
                            putExtra("extra_subject_id", upcomingSubject.id)
                        }
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                    if (upcomingSubject != null) {
                        val countdown = DateUtils.getCountdownInfo(upcomingSubject.examDate)
                        views.setTextViewText(R.id.widget_countdown_number, countdown.displayLabel)
                        views.setTextViewText(R.id.widget_subject_name, upcomingSubject.name)
                        views.setTextViewText(
                            R.id.widget_exam_date,
                            DateUtils.formatExamDate(upcomingSubject.examDate)
                        )

                        views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_progress_text, View.VISIBLE)
                        views.setProgressBar(R.id.widget_progress_bar, 100, progressPct, false)
                        views.setTextViewText(
                            R.id.widget_progress_text,
                            if (totalTopics > 0) "$doneTopics of $totalTopics done ($progressPct%)" else "No topics added yet"
                        )

                        if (countdown.status == CountdownStatus.TODAY) {
                            views.setTextViewText(R.id.widget_label, "TODAY'S EXAM")
                        } else if (countdown.status == CountdownStatus.URGENT) {
                            views.setTextViewText(R.id.widget_label, "UPCOMING SOON")
                        } else {
                            views.setTextViewText(R.id.widget_label, "NEXT EXAM")
                        }
                    } else {
                        views.setTextViewText(R.id.widget_label, "EXAMTRACK")
                        views.setTextViewText(R.id.widget_countdown_number, "No Exams")
                        views.setTextViewText(R.id.widget_subject_name, "Tap to add your first exam")
                        views.setTextViewText(R.id.widget_exam_date, "Offline & ready to track")
                        views.setViewVisibility(R.id.widget_progress_bar, View.GONE)
                        views.setViewVisibility(R.id.widget_progress_text, View.GONE)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
