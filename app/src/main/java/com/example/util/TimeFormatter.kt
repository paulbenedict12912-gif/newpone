package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class TimeComponents(
    val hours: String,
    val minutes: String,
    val seconds: String,
    val hundredths: String,
    val totalHoursInt: Long
)

object TimeFormatter {

    fun splitComponents(timeMillis: Long): TimeComponents {
        val totalMillis = if (timeMillis < 0) 0L else timeMillis
        val hours = totalMillis / 3600000L
        val minutes = (totalMillis % 3600000L) / 60000L
        val seconds = (totalMillis % 60000L) / 1000L
        val hundredths = (totalMillis % 1000L) / 10L

        return TimeComponents(
            hours = String.format(Locale.US, "%02d", hours),
            minutes = String.format(Locale.US, "%02d", minutes),
            seconds = String.format(Locale.US, "%02d", seconds),
            hundredths = String.format(Locale.US, "%02d", hundredths),
            totalHoursInt = hours
        )
    }

    fun format(timeMillis: Long, includeHundredths: Boolean = true): String {
        val c = splitComponents(timeMillis)
        return if (c.totalHoursInt > 0) {
            if (includeHundredths) {
                "${c.hours}:${c.minutes}:${c.seconds}.${c.hundredths}"
            } else {
                "${c.hours}:${c.minutes}:${c.seconds}"
            }
        } else {
            if (includeHundredths) {
                "${c.minutes}:${c.seconds}.${c.hundredths}"
            } else {
                "${c.minutes}:${c.seconds}"
            }
        }
    }

    fun formatLapDiff(diffMillis: Long): String {
        val sign = if (diffMillis >= 0) "+" else "-"
        val absMillis = abs(diffMillis)
        val minutes = (absMillis % 3600000L) / 60000L
        val seconds = (absMillis % 60000L) / 1000L
        val hundredths = (absMillis % 1000L) / 10L
        return "$sign${String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)}"
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
