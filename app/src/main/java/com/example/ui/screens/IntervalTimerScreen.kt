package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TimerPreset
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DialRim
import com.example.ui.theme.StopwatchAmber
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.StopwatchGreen
import com.example.ui.theme.StopwatchRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeFormatter

@Composable
fun IntervalTimerScreen(
    totalMillis: Long,
    remainingMillis: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    isFinished: Boolean,
    presets: List<TimerPreset>,
    onSelectPreset: (Long) -> Unit,
    onAdjustTime: (Long) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val progress = if (totalMillis > 0) {
        (remainingMillis.toFloat() / totalMillis.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Flashing transition when finished
    val infiniteTransition = rememberInfiniteTransition(label = "finishedPulse")
    val finishedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "finishedPulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Quick Presets Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "QUICK PRESETS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isSelected = totalMillis == preset.durationSeconds * 1000L
                    Card(
                        modifier = Modifier
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSelectPreset(preset.durationSeconds)
                            }
                            .testTag("preset_${preset.title}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) StopwatchCyan.copy(alpha = 0.2f) else DarkSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) StopwatchCyan else DarkSurfaceBorder
                        )
                    ) {
                        Text(
                            text = preset.title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StopwatchCyan else TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Circular Countdown Display
        Box(
            modifier = Modifier
                .size(260.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension / 2f) - strokeWidth

                // Background track
                drawCircle(
                    color = DialRim,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground remaining arc
                val sweep = progress * 360f
                val arcColor = when {
                    isFinished -> StopwatchRed
                    remainingMillis <= 5000L -> StopwatchAmber
                    else -> StopwatchCyan
                }

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(arcColor.copy(alpha = 0.7f), arcColor),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central time readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isFinished) {
                    Text(
                        text = "TIME'S UP!",
                        fontWeight = FontWeight.Black,
                        color = StopwatchRed,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.alpha(finishedAlpha)
                    )
                }

                val components = TimeFormatter.splitComponents(remainingMillis)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (components.totalHoursInt > 0) {
                        Text(
                            text = components.hours,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = ":",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                            color = StopwatchCyan
                        )
                    }
                    Text(
                        text = components.minutes,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = ":",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        color = StopwatchCyan
                    )
                    Text(
                        text = components.seconds,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = if (isRunning) "REMAINING" else if (isPaused) "PAUSED" else "TARGET",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Stepper Quick-Adjust Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdjustButton(label = "-1m", onClick = { onAdjustTime(-60L) })
            AdjustButton(label = "-10s", onClick = { onAdjustTime(-10L) })
            AdjustButton(label = "+10s", onClick = { onAdjustTime(10L) })
            AdjustButton(label = "+1m", onClick = { onAdjustTime(60L) })
        }

        // Action Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onReset()
                },
                modifier = Modifier
                    .size(72.dp)
                    .testTag("timer_reset_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Reset Timer",
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Start / Pause Primary Button
            if (isRunning) {
                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPause()
                    },
                    modifier = Modifier
                        .size(88.dp)
                        .testTag("timer_pause_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = StopwatchAmber)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = "Pause Timer",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onStart()
                    },
                    modifier = Modifier
                        .size(88.dp)
                        .testTag("timer_start_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = StopwatchGreen)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Start Timer",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustButton(
    label: String,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Card(
        modifier = Modifier
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .testTag("adjust_$label"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
