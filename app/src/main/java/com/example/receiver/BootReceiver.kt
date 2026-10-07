package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.ExamTrackDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = ExamTrackDatabase.getDatabase(context)
                    val subjects = db.subjectDao().getAllSubjectsSync()
                    for (subject in subjects) {
                        NotificationHelper.scheduleExamReminders(context, subject)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
