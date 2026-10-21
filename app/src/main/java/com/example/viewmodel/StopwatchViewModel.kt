package com.example.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.SessionEntity
import com.example.model.AppTab
import com.example.model.LapItem
import com.example.model.StopwatchStats
import com.example.model.TimerPreset
import com.example.util.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionDao = AppDatabase.getInstance(application).sessionDao()

    val savedSessions: StateFlow<List<SessionEntity>> = sessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs
    private val _currentTab = MutableStateFlow(AppTab.STOPWATCH)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Stopwatch State
    private val _elapsedTimeMillis = MutableStateFlow(0L)
    val elapsedTimeMillis: StateFlow<Long> = _elapsedTimeMillis.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _laps = MutableStateFlow<List<LapItem>>(emptyList())
    val laps: StateFlow<List<LapItem>> = _laps.asStateFlow()

    private val _stats = MutableStateFlow(StopwatchStats())
    val stats: StateFlow<StopwatchStats> = _stats.asStateFlow()

    // Preferences & UI Modes
    private val _keepScreenOn = MutableStateFlow(true)
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    // Internal timing job
    private var stopwatchJob: Job? = null
    private var baseTimestamp: Long = 0L

    // Countdown Timer State
    val timerPresets = listOf(
        TimerPreset("30s", 30L),
        TimerPreset("1m", 60L),
        TimerPreset("2m", 120L),
        TimerPreset("3m", 180L),
        TimerPreset("5m", 300L),
        TimerPreset("10m", 600L),
        TimerPreset("15m", 900L)
    )

    private val _countdownTotalMillis = MutableStateFlow(60_000L) // Default 1 min
    val countdownTotalMillis: StateFlow<Long> = _countdownTotalMillis.asStateFlow()

    private val _countdownRemainingMillis = MutableStateFlow(60_000L)
    val countdownRemainingMillis: StateFlow<Long> = _countdownRemainingMillis.asStateFlow()

    private val _isCountdownRunning = MutableStateFlow(false)
    val isCountdownRunning: StateFlow<Boolean> = _isCountdownRunning.asStateFlow()

    private val _isCountdownPaused = MutableStateFlow(false)
    val isCountdownPaused: StateFlow<Boolean> = _isCountdownPaused.asStateFlow()

    private val _isCountdownFinished = MutableStateFlow(false)
    val isCountdownFinished: StateFlow<Boolean> = _isCountdownFinished.asStateFlow()

    private var countdownJob: Job? = null
    private var countdownEndTimestamp: Long = 0L

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleKeepScreenOn() {
        _keepScreenOn.value = !_keepScreenOn.value
    }

    fun toggleHaptics() {
        _hapticsEnabled.value = !_hapticsEnabled.value
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun setShowSaveDialog(show: Boolean) {
        _showSaveDialog.value = show
    }

    // --- Stopwatch actions ---
    fun startStopwatch() {
        if (_isRunning.value) return

        _isRunning.value = true
        _isPaused.value = false
        baseTimestamp = SystemClock.elapsedRealtime() - _elapsedTimeMillis.value

        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            while (_isRunning.value) {
                _elapsedTimeMillis.value = SystemClock.elapsedRealtime() - baseTimestamp
                delay(16) // Smooth 60fps refresh
            }
        }
    }

    fun pauseStopwatch() {
        if (!_isRunning.value) return
        _isRunning.value = false
        _isPaused.value = true
        stopwatchJob?.cancel()
        _elapsedTimeMillis.value = SystemClock.elapsedRealtime() - baseTimestamp
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _isRunning.value = false
        _isPaused.value = false
        _elapsedTimeMillis.value = 0L
        _laps.value = emptyList()
        _stats.value = StopwatchStats()
    }

    fun recordLap() {
        val currentTotal = _elapsedTimeMillis.value
        val currentLaps = _laps.value
        val lastTotal = if (currentLaps.isEmpty()) 0L else currentLaps.first().totalTimeMillis
        val lapDuration = currentTotal - lastTotal

        val newLapNumber = currentLaps.size + 1
        val newLap = LapItem(
            lapNumber = newLapNumber,
            lapTimeMillis = lapDuration,
            totalTimeMillis = currentTotal
        )

        // Prepend to show latest lap first
        val updatedLaps = listOf(newLap) + currentLaps
        recalculateLapHighlights(updatedLaps)
    }

    private fun recalculateLapHighlights(rawLaps: List<LapItem>) {
        if (rawLaps.isEmpty()) {
            _laps.value = emptyList()
            _stats.value = StopwatchStats()
            return
        }

        val totalLaps = rawLaps.size
        var fastestTime = Long.MAX_VALUE
        var slowestTime = Long.MIN_VALUE
        var totalLapDuration = 0L

        rawLaps.forEach { lap ->
            if (lap.lapTimeMillis < fastestTime) fastestTime = lap.lapTimeMillis
            if (lap.lapTimeMillis > slowestTime) slowestTime = lap.lapTimeMillis
            totalLapDuration += lap.lapTimeMillis
        }

        val avg = totalLapDuration / totalLaps

        // Only mark fastest & slowest if at least 2 laps exist
        val processedLaps = rawLaps.map { lap ->
            val isFastest = totalLaps >= 2 && lap.lapTimeMillis == fastestTime
            val isSlowest = totalLaps >= 2 && lap.lapTimeMillis == slowestTime
            val diff = lap.lapTimeMillis - avg
            lap.copy(
                isFastest = isFastest,
                isSlowest = isSlowest,
                diffFromAvgMillis = diff
            )
        }

        _laps.value = processedLaps
        _stats.value = StopwatchStats(
            totalLaps = totalLaps,
            fastestLapTimeMillis = if (totalLaps >= 2) fastestTime else null,
            slowestLapTimeMillis = if (totalLaps >= 2) slowestTime else null,
            averageLapTimeMillis = avg
        )
    }

    fun saveCurrentSession(title: String, notes: String) {
        val totalMillis = _elapsedTimeMillis.value
        val lapList = _laps.value
        val lapCount = lapList.size

        // Build formatted summary text for sharing/details
        val summaryBuilder = StringBuilder()
        lapList.reversed().forEach { lap ->
            summaryBuilder.append("Lap ${lap.lapNumber}: ${TimeFormatter.format(lap.lapTimeMillis)} (Total: ${TimeFormatter.format(lap.totalTimeMillis)})\n")
        }

        val entity = SessionEntity(
            title = if (title.isBlank()) "Session ${TimeFormatter.formatDateTime(System.currentTimeMillis())}" else title.trim(),
            notes = notes.trim(),
            timestamp = System.currentTimeMillis(),
            totalDurationMillis = totalMillis,
            lapsCount = lapCount,
            fastestLapMillis = _stats.value.fastestLapTimeMillis,
            slowestLapMillis = _stats.value.slowestLapTimeMillis,
            lapsData = summaryBuilder.toString()
        )

        viewModelScope.launch {
            sessionDao.insertSession(entity)
            _showSaveDialog.value = false
        }
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            sessionDao.deleteSession(session)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            sessionDao.clearAll()
        }
    }

    // --- Countdown Timer actions ---
    fun setCountdownDuration(seconds: Long) {
        if (_isCountdownRunning.value) return
        val millis = (seconds * 1000L).coerceAtLeast(1000L)
        _countdownTotalMillis.value = millis
        _countdownRemainingMillis.value = millis
        _isCountdownPaused.value = false
        _isCountdownFinished.value = false
    }

    fun addCountdownSeconds(seconds: Long) {
        if (_isCountdownFinished.value) {
            _isCountdownFinished.value = false
        }
        val addedMillis = seconds * 1000L
        val newRemaining = (_countdownRemainingMillis.value + addedMillis).coerceAtLeast(1000L)
        _countdownRemainingMillis.value = newRemaining
        if (!_isCountdownRunning.value) {
            _countdownTotalMillis.value = newRemaining
        } else {
            countdownEndTimestamp += addedMillis
        }
    }

    fun startCountdown() {
        if (_isCountdownRunning.value) return
        if (_countdownRemainingMillis.value <= 0L) {
            _countdownRemainingMillis.value = _countdownTotalMillis.value
        }
        _isCountdownRunning.value = true
        _isCountdownPaused.value = false
        _isCountdownFinished.value = false

        countdownEndTimestamp = SystemClock.elapsedRealtime() + _countdownRemainingMillis.value

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_isCountdownRunning.value) {
                val left = countdownEndTimestamp - SystemClock.elapsedRealtime()
                if (left <= 0) {
                    _countdownRemainingMillis.value = 0L
                    _isCountdownRunning.value = false
                    _isCountdownPaused.value = false
                    _isCountdownFinished.value = true
                    break
                } else {
                    _countdownRemainingMillis.value = left
                }
                delay(33) // ~30fps for countdown ring
            }
        }
    }

    fun pauseCountdown() {
        if (!_isCountdownRunning.value) return
        _isCountdownRunning.value = false
        _isCountdownPaused.value = true
        countdownJob?.cancel()
        val left = (countdownEndTimestamp - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        _countdownRemainingMillis.value = left
    }

    fun resetCountdown() {
        countdownJob?.cancel()
        _isCountdownRunning.value = false
        _isCountdownPaused.value = false
        _isCountdownFinished.value = false
        _countdownRemainingMillis.value = _countdownTotalMillis.value
    }

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
        countdownJob?.cancel()
    }
}
