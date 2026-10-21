package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LapItem
import com.example.ui.components.DigitalDisplay
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StopwatchAmber
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.StopwatchGreen
import com.example.ui.theme.StopwatchRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.TimeFormatter

@Composable
fun FullscreenStopwatchDialog(
    elapsedTimeMillis: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    laps: List<LapItem>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ZEN TRACK MODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 2.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .testTag("exit_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Exit Fullscreen",
                            tint = TextPrimary
                        )
                    }
                }

                // Giant Center Digital Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DigitalDisplay(
                        elapsedTimeMillis = elapsedTimeMillis,
                        isRunning = isRunning,
                        isPaused = isPaused,
                        mainFontSize = 64.sp,
                        subFontSize = 32.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (laps.isNotEmpty()) {
                        val latestLap = laps.first()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Lap #${latestLap.lapNumber}: ",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                            Text(
                                text = TimeFormatter.format(latestLap.lapTimeMillis),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = StopwatchCyan,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Big Ergonomic Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRunning) {
                        // Lap Button
                        Button(
                            onClick = onLap,
                            modifier = Modifier
                                .size(88.dp)
                                .testTag("fullscreen_lap_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Flag,
                                    contentDescription = "Lap",
                                    tint = StopwatchCyan
                                )
                                Text("LAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StopwatchCyan)
                            }
                        }

                        // Stop Button
                        Button(
                            onClick = onPause,
                            modifier = Modifier
                                .size(96.dp)
                                .testTag("fullscreen_pause_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = StopwatchRed)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Pause,
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    } else if (isPaused) {
                        // Reset Button
                        Button(
                            onClick = onReset,
                            modifier = Modifier
                                .size(88.dp)
                                .testTag("fullscreen_reset_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Reset",
                                    tint = TextMuted
                                )
                                Text("RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            }
                        }

                        // Resume Button
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .size(96.dp)
                                .testTag("fullscreen_resume_button"),
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
                                Text("RESUME", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    } else {
                        // Start Button (Idle)
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .size(100.dp)
                                .testTag("fullscreen_start_button"),
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
                                Text("START", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
