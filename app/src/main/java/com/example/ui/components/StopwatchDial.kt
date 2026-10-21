package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.LapItem
import com.example.ui.theme.DialRim
import com.example.ui.theme.DialTickMajor
import com.example.ui.theme.DialTickMinor
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.StopwatchGreen
import com.example.ui.theme.StopwatchRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StopwatchDial(
    elapsedTimeMillis: Long,
    isRunning: Boolean,
    laps: List<LapItem>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    // Current seconds and fractional part (0..60s)
    val secondsWithFraction = (elapsedTimeMillis % 60000L) / 1000f
    val secondsAngle = (secondsWithFraction / 60f) * 360f

    // Animated angle for smooth needle sweep
    val needleAngle by animateFloatAsState(
        targetValue = secondsAngle,
        animationSpec = tween(durationMillis = if (isRunning) 50 else 0, easing = LinearEasing),
        label = "NeedleAngle"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 16.dp.toPx()

            // 1. Draw outer rim & background dial plate
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B).copy(alpha = 0.35f),
                        Color(0xFF0F172A).copy(alpha = 0.85f)
                    ),
                    center = center,
                    radius = radius + 12.dp.toPx()
                ),
                radius = radius + 10.dp.toPx(),
                center = center
            )

            // Outer dial track
            drawCircle(
                color = DialRim,
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )

            // 2. Dynamic progress arc along the rim (elapsed seconds in current minute)
            val sweepAngle = (secondsWithFraction / 60f) * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            StopwatchCyan.copy(alpha = 0.4f),
                            StopwatchCyan,
                            Color(0xFF38BDF8)
                        ),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Draw 60 Dial Ticks (Seconds marks)
            for (i in 0 until 60) {
                val isMajor = (i % 5 == 0)
                val angleRad = (i * 6 - 90) * (PI / 180f).toFloat()

                val tickLength = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                val tickWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                val tickColor = if (isMajor) DialTickMajor else DialTickMinor

                val startX = center.x + (radius - tickLength) * cos(angleRad)
                val startY = center.y + (radius - tickLength) * sin(angleRad)
                val endX = center.x + radius * cos(angleRad)
                val endY = center.y + radius * sin(angleRad)

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // 4. Draw Lap Markers on the rim
            laps.take(12).forEach { lap ->
                val lapSec = (lap.totalTimeMillis % 60000L) / 1000f
                val lapAngleRad = (lapSec * 6 - 90) * (PI / 180f).toFloat()
                val dotRadius = 4.dp.toPx()
                val dotX = center.x + (radius + 6.dp.toPx()) * cos(lapAngleRad)
                val dotY = center.y + (radius + 6.dp.toPx()) * sin(lapAngleRad)

                val dotColor = when {
                    lap.isFastest -> StopwatchGreen
                    lap.isSlowest -> StopwatchRed
                    else -> StopwatchCyan
                }

                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(dotX, dotY)
                )
            }

            // 5. Draw Seconds Needle Hand
            val needleRad = (needleAngle - 90) * (PI / 180f).toFloat()
            val needleLength = radius - 20.dp.toPx()
            val needleEndX = center.x + needleLength * cos(needleRad)
            val needleEndY = center.y + needleLength * sin(needleRad)

            // Counter balance back tail
            val tailLength = 22.dp.toPx()
            val tailStartX = center.x - tailLength * cos(needleRad)
            val tailStartY = center.y - tailLength * sin(needleRad)

            // Tail
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(tailStartX, tailStartY),
                end = center,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Main needle
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(StopwatchCyan, Color(0xFF67E8F9)),
                    start = center,
                    end = Offset(needleEndX, needleEndY)
                ),
                start = center,
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Needle tip glowing accent dot
            drawCircle(
                color = StopwatchCyan,
                radius = 4.5.dp.toPx(),
                center = Offset(needleEndX, needleEndY)
            )

            // Center pivot cap
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                color = StopwatchCyan,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Inner Composable content (e.g. Digital Display)
        content()
    }
}
