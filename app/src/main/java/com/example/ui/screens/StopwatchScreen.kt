package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.LapItem
import com.example.model.StopwatchStats
import com.example.ui.components.DigitalDisplay
import com.example.ui.components.LapListView
import com.example.ui.components.StopwatchDial
import com.example.ui.dialogs.SaveSessionDialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StopwatchAmber
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.StopwatchGreen
import com.example.ui.theme.StopwatchRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.StopwatchViewModel

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val currentTab by viewModel.currentTab.collectAsState()
    val elapsedTimeMillis by viewModel.elapsedTimeMillis.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val laps by viewModel.laps.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()

    // Countdown state
    val countdownTotalMillis by viewModel.countdownTotalMillis.collectAsState()
    val countdownRemainingMillis by viewModel.countdownRemainingMillis.collectAsState()
    val isCountdownRunning by viewModel.isCountdownRunning.collectAsState()
    val isCountdownPaused by viewModel.isCountdownPaused.collectAsState()
    val isCountdownFinished by viewModel.isCountdownFinished.collectAsState()

    // Sessions
    val savedSessions by viewModel.savedSessions.collectAsState()

    fun triggerHaptic() {
        if (hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBarRow(
                currentTab = currentTab,
                keepScreenOn = keepScreenOn,
                hapticsEnabled = hapticsEnabled,
                onToggleKeepScreenOn = {
                    triggerHaptic()
                    viewModel.toggleKeepScreenOn()
                },
                onToggleHaptics = {
                    triggerHaptic()
                    viewModel.toggleHaptics()
                },
                onToggleFullscreen = {
                    triggerHaptic()
                    viewModel.toggleFullscreen()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Navigation Tabs
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = DarkBackground,
                contentColor = StopwatchCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = StopwatchCyan,
                        height = 3.dp
                    )
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkSurfaceBorder)
                    )
                }
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = {
                            triggerHaptic()
                            viewModel.setTab(tab)
                        },
                        text = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) StopwatchCyan else TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Tab Content
            when (currentTab) {
                AppTab.STOPWATCH -> {
                    StopwatchMainTab(
                        elapsedTimeMillis = elapsedTimeMillis,
                        isRunning = isRunning,
                        isPaused = isPaused,
                        laps = laps,
                        stats = stats,
                        onStart = {
                            triggerHaptic()
                            viewModel.startStopwatch()
                        },
                        onPause = {
                            triggerHaptic()
                            viewModel.pauseStopwatch()
                        },
                        onReset = {
                            triggerHaptic()
                            viewModel.resetStopwatch()
                        },
                        onLap = {
                            triggerHaptic()
                            viewModel.recordLap()
                        },
                        onOpenSave = {
                            triggerHaptic()
                            viewModel.setShowSaveDialog(true)
                        },
                        onOpenFullscreen = {
                            triggerHaptic()
                            viewModel.toggleFullscreen()
                        }
                    )
                }

                AppTab.TIMER -> {
                    IntervalTimerScreen(
                        totalMillis = countdownTotalMillis,
                        remainingMillis = countdownRemainingMillis,
                        isRunning = isCountdownRunning,
                        isPaused = isCountdownPaused,
                        isFinished = isCountdownFinished,
                        presets = viewModel.timerPresets,
                        onSelectPreset = { seconds ->
                            viewModel.setCountdownDuration(seconds)
                        },
                        onAdjustTime = { deltaSeconds ->
                            viewModel.addCountdownSeconds(deltaSeconds)
                        },
                        onStart = {
                            viewModel.startCountdown()
                        },
                        onPause = {
                            viewModel.pauseCountdown()
                        },
                        onReset = {
                            viewModel.resetCountdown()
                        }
                    )
                }

                AppTab.HISTORY -> {
                    HistoryScreen(
                        sessions = savedSessions,
                        onDeleteSession = { session ->
                            triggerHaptic()
                            viewModel.deleteSession(session)
                        },
                        onClearAll = {
                            triggerHaptic()
                            viewModel.clearAllHistory()
                        }
                    )
                }
            }
        }
    }

    // Fullscreen Overlay Dialog
    if (isFullscreen) {
        FullscreenStopwatchDialog(
            elapsedTimeMillis = elapsedTimeMillis,
            isRunning = isRunning,
            isPaused = isPaused,
            laps = laps,
            onStart = {
                triggerHaptic()
                viewModel.startStopwatch()
            },
            onPause = {
                triggerHaptic()
                viewModel.pauseStopwatch()
            },
            onReset = {
                triggerHaptic()
                viewModel.resetStopwatch()
            },
            onLap = {
                triggerHaptic()
                viewModel.recordLap()
            },
            onDismiss = {
                triggerHaptic()
                viewModel.toggleFullscreen()
            }
        )
    }

    // Save Session Modal Dialog
    if (showSaveDialog) {
        SaveSessionDialog(
            elapsedTimeMillis = elapsedTimeMillis,
            lapsCount = laps.size,
            onDismiss = { viewModel.setShowSaveDialog(false) },
            onSave = { title, notes ->
                triggerHaptic()
                viewModel.saveCurrentSession(title, notes)
            }
        )
    }
}

@Composable
private fun TopAppBarRow(
    currentTab: AppTab,
    keepScreenOn: Boolean,
    hapticsEnabled: Boolean,
    onToggleKeepScreenOn: () -> Unit,
    onToggleHaptics: () -> Unit,
    onToggleFullscreen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(StopwatchCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = "Stopwatch Logo",
                    tint = StopwatchCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "CHRONO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Precision Stopwatch",
                    style = MaterialTheme.typography.labelSmall,
                    color = StopwatchCyan,
                    fontSize = 10.sp
                )
            }
        }

        // Action icons
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Keep Screen On Toggle
            IconButton(
                onClick = onToggleKeepScreenOn,
                modifier = Modifier.testTag("keep_screen_on_toggle")
            ) {
                Icon(
                    imageVector = Icons.Filled.StayCurrentPortrait,
                    contentDescription = "Keep Screen On",
                    tint = if (keepScreenOn) StopwatchCyan else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Haptics Toggle
            IconButton(
                onClick = onToggleHaptics,
                modifier = Modifier.testTag("haptics_toggle")
            ) {
                Icon(
                    imageVector = Icons.Filled.Vibration,
                    contentDescription = "Toggle Haptics",
                    tint = if (hapticsEnabled) StopwatchCyan else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Fullscreen Toggle
            if (currentTab == AppTab.STOPWATCH) {
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier.testTag("open_fullscreen_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fullscreen,
                        contentDescription = "Zen Fullscreen Mode",
                        tint = TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StopwatchMainTab(
    elapsedTimeMillis: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    laps: List<LapItem>,
    stats: com.example.model.StopwatchStats,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    onOpenSave: () -> Unit,
    onOpenFullscreen: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dial with embedded Digital Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable { onOpenFullscreen() }
                .testTag("stopwatch_dial_container"),
            contentAlignment = Alignment.Center
        ) {
            StopwatchDial(
                elapsedTimeMillis = elapsedTimeMillis,
                isRunning = isRunning,
                laps = laps,
                modifier = Modifier.size(260.dp)
            ) {
                DigitalDisplay(
                    elapsedTimeMillis = elapsedTimeMillis,
                    isRunning = isRunning,
                    isPaused = isPaused,
                    mainFontSize = 36.sp,
                    subFontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Primary Control Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                isRunning -> {
                    // Lap Button
                    Button(
                        onClick = onLap,
                        modifier = Modifier
                            .size(76.dp)
                            .testTag("lap_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Flag,
                                contentDescription = "Lap",
                                tint = StopwatchCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("LAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StopwatchCyan)
                        }
                    }

                    // Stop Button
                    Button(
                        onClick = onPause,
                        modifier = Modifier
                            .size(86.dp)
                            .testTag("stop_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = StopwatchRed)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }

                isPaused -> {
                    // Reset Button
                    Button(
                        onClick = onReset,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("reset_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset",
                                tint = TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("RESET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        }
                    }

                    // Resume Button
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .size(86.dp)
                            .testTag("resume_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = StopwatchGreen)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Resume",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                            Text("RESUME", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }

                    // Save Session Button
                    Button(
                        onClick = onOpenSave,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("save_session_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkBorder,
                                contentDescription = "Save",
                                tint = StopwatchAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Text("SAVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StopwatchAmber)
                        }
                    }
                }

                else -> {
                    // Idle state
                    // Disabled Lap Button
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("disabled_lap_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = DarkSurfaceElevated.copy(alpha = 0.5f),
                            disabledContentColor = TextMuted.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Text("LAP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Start Button
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .size(86.dp)
                            .testTag("start_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = StopwatchGreen)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Start",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                            Text("START", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scrollable Lap List & Splits Area
        LapListView(
            laps = laps,
            stats = stats,
            totalElapsedMillis = elapsedTimeMillis,
            modifier = Modifier.fillMaxSize()
        )
    }
}
