package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class CountdownStatus {
    PASSED,
    TODAY,
    URGENT,   // 1 to 3 days left
    UPCOMING  // 4+ days left
}

data class CountdownInfo(
    val daysRemaining: Int,
    val status: CountdownStatus,
    val displayLabel: String,
    val shortLabel: String
)

object DateUtils {

    private val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    fun formatExamDate(timestamp: Long): String {
        return displayFormat.format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    /**
     * Truncates a timestamp to 00:00:00.000 (start of day) in the current default timezone.
     */
    fun startOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Calculates days difference strictly by calendar days between today and the target exam date.
     */
    fun getCountdownInfo(examDateMillis: Long, currentMillis: Long = System.currentTimeMillis()): CountdownInfo {
        val todayStart = startOfDay(currentMillis)
        val examStart = startOfDay(examDateMillis)

        val diffMillis = examStart - todayStart
        val days = (diffMillis / TimeUnit.DAYS.toMillis(1)).toInt()

        return when {
            days < 0 -> CountdownInfo(
                daysRemaining = days,
                status = CountdownStatus.PASSED,
                displayLabel = "Exam passed",
                shortLabel = "Passed"
            )
            days == 0 -> CountdownInfo(
                daysRemaining = 0,
                status = CountdownStatus.TODAY,
                displayLabel = "Today!",
                shortLabel = "Today!"
            )
            days == 1 -> CountdownInfo(
                daysRemaining = 1,
                status = CountdownStatus.URGENT,
                displayLabel = "1 day left",
                shortLabel = "1 day left"
            )
            days in 2..3 -> CountdownInfo(
                daysRemaining = days,
                status = CountdownStatus.URGENT,
                displayLabel = "$days days left",
                shortLabel = "${days}d left"
            )
            else -> CountdownInfo(
                daysRemaining = days,
                status = CountdownStatus.UPCOMING,
                displayLabel = "$days days left",
                shortLabel = "${days}d left"
            )
        }
    }
}
