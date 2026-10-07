package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notification.NotificationHelper

class ExamNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val subjectId = intent.getStringExtra(NotificationHelper.EXTRA_SUBJECT_ID) ?: return
        val subjectName = intent.getStringExtra(NotificationHelper.EXTRA_SUBJECT_NAME) ?: "Exam"
        val daysLeft = intent.getIntExtra(NotificationHelper.EXTRA_DAYS_LEFT, 1)

        NotificationHelper.showNotification(context, subjectId, subjectName, daysLeft)
    }
}
