package com.example.model

data class LapItem(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long,
    val isFastest: Boolean = false,
    val isSlowest: Boolean = false,
    val diffFromAvgMillis: Long = 0L
)

data class StopwatchStats(
    val totalLaps: Int = 0,
    val fastestLapTimeMillis: Long? = null,
    val slowestLapTimeMillis: Long? = null,
    val averageLapTimeMillis: Long? = null
)

enum class AppTab(val title: String) {
    STOPWATCH("Stopwatch"),
    TIMER("Timer"),
    HISTORY("History")
}

data class TimerPreset(
    val title: String,
    val durationSeconds: Long
)
