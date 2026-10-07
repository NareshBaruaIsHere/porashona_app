package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Subject
import com.example.receiver.ExamNotificationReceiver
import com.example.util.DateUtils
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    const val CHANNEL_ID = "exam_reminders_channel"
    const val EXTRA_SUBJECT_ID = "extra_subject_id"
    const val EXTRA_SUBJECT_NAME = "extra_subject_name"
    const val EXTRA_DAYS_LEFT = "extra_days_left"

    private val REMINDER_DAYS = listOf(7, 3, 1)

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Exam Reminders"
            val descriptionText = "Study reminders 7, 3, and 1 day before upcoming exams"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleExamReminders(context: Context, subject: Subject) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val examStartOfDay = DateUtils.startOfDay(subject.examDate)

        for (daysBefore in REMINDER_DAYS) {
            val reminderCal = Calendar.getInstance().apply {
                timeInMillis = examStartOfDay - TimeUnit.DAYS.toMillis(daysBefore.toLong())
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val triggerTime = reminderCal.timeInMillis
            if (triggerTime > System.currentTimeMillis()) {
                val intent = Intent(context, ExamNotificationReceiver::class.java).apply {
                    putExtra(EXTRA_SUBJECT_ID, subject.id)
                    putExtra(EXTRA_SUBJECT_NAME, subject.name)
                    putExtra(EXTRA_DAYS_LEFT, daysBefore)
                }

                val requestCode = getRequestCode(subject.id, daysBefore)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    // Fallback if exact alarms permission restricted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        }
    }

    fun cancelExamReminders(context: Context, subjectId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (daysBefore in REMINDER_DAYS) {
            val intent = Intent(context, ExamNotificationReceiver::class.java)
            val requestCode = getRequestCode(subjectId, daysBefore)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    fun showNotification(context: Context, subjectId: String, subjectName: String, daysLeft: Int) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SUBJECT_ID, subjectId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            subjectId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (daysLeft == 1) {
            "Tomorrow is your $subjectName exam!"
        } else {
            "$daysLeft days left for $subjectName!"
        }

        val message = "Keep up the momentum. Check off syllabus topics to stay prepared."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(subjectId.hashCode() + daysLeft, builder.build())
        } catch (e: SecurityException) {
            // Notifications permission not granted
        }
    }

    private fun getRequestCode(subjectId: String, daysBefore: Int): Int {
        return (subjectId.hashCode() and 0x0FFF) * 10 + daysBefore
    }
}
