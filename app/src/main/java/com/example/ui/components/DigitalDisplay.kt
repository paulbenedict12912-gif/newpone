package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StopwatchAmber
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.StopwatchGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.TimeFormatter

@Composable
fun DigitalDisplay(
    elapsedTimeMillis: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    modifier: Modifier = Modifier,
    mainFontSize: TextUnit = 48.sp,
    subFontSize: TextUnit = 24.sp
) {
    val components = TimeFormatter.splitComponents(elapsedTimeMillis)

    // Pulsing indicator for active timer
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val statusText = when {
        isRunning -> "RUNNING"
        isPaused -> "PAUSED"
        else -> "READY"
    }

    val statusColor = when {
        isRunning -> StopwatchGreen
        isPaused -> StopwatchAmber
        else -> TextMuted
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated.copy(alpha = 0.85f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .alpha(if (isRunning) pulseAlpha else 1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Numbers
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            if (components.totalHoursInt > 0) {
                Text(
                    text = components.hours,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = mainFontSize,
                    color = TextPrimary
                )
                Text(
                    text = ":",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = mainFontSize,
                    color = StopwatchCyan.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            Text(
                text = components.minutes,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = mainFontSize,
                color = TextPrimary
            )

            Text(
                text = ":",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = mainFontSize,
                color = StopwatchCyan.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            Text(
                text = components.seconds,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = mainFontSize,
                color = TextPrimary
            )

            // Milliseconds / Hundredths
            Text(
                text = ".",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = subFontSize,
                color = StopwatchCyan,
                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
            )
            Text(
                text = components.hundredths,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = subFontSize,
                color = StopwatchCyan,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}
