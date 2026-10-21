package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LapItem
import com.example.model.StopwatchStats
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.StopwatchGreen
import com.example.ui.theme.StopwatchRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.TimeFormatter

@Composable
fun LapListView(
    laps: List<LapItem>,
    stats: StopwatchStats,
    totalElapsedMillis: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = modifier) {
        if (laps.isNotEmpty()) {
            // Stats summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurfaceElevated.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatPill(
                        label = "Fastest",
                        value = stats.fastestLapTimeMillis?.let { TimeFormatter.format(it) } ?: "--:--",
                        accentColor = StopwatchGreen
                    )
                    StatPill(
                        label = "Slowest",
                        value = stats.slowestLapTimeMillis?.let { TimeFormatter.format(it) } ?: "--:--",
                        accentColor = StopwatchRed
                    )
                    StatPill(
                        label = "Average",
                        value = stats.averageLapTimeMillis?.let { TimeFormatter.format(it) } ?: "--:--",
                        accentColor = StopwatchCyan
                    )
                }
            }

            // Lap List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = "Laps",
                        tint = StopwatchCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LAPS (${laps.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val summary = buildSummary(laps, totalElapsedMillis)
                            clipboardManager.setText(AnnotatedString(summary))
                            Toast.makeText(context, "Laps copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_laps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Laps",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val summary = buildSummary(laps, totalElapsedMillis)
                            shareLaps(context, summary)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_laps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share Laps",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Columns title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "LAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = "SPLIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // List of laps
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(laps, key = { it.lapNumber }) { lap ->
                    LapRowItem(lap = lap)
                }
            }
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No laps recorded yet",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Lap' while timing to record split times",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = accentColor
        )
    }
}

@Composable
private fun LapRowItem(lap: LapItem) {
    val borderColor = when {
        lap.isFastest -> StopwatchGreen.copy(alpha = 0.4f)
        lap.isSlowest -> StopwatchRed.copy(alpha = 0.4f)
        else -> DarkSurfaceBorder.copy(alpha = 0.5f)
    }

    val backgroundColor = when {
        lap.isFastest -> StopwatchGreen.copy(alpha = 0.08f)
        lap.isSlowest -> StopwatchRed.copy(alpha = 0.08f)
        else -> DarkSurface.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lap Number + Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = String.format("#%02d", lap.lapNumber),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (lap.isFastest) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StopwatchGreen)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "BEST",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                } else if (lap.isSlowest) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StopwatchRed)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "SLOW",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            // Split Duration + Delta from Avg
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = TimeFormatter.format(lap.lapTimeMillis),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = when {
                        lap.isFastest -> StopwatchGreen
                        lap.isSlowest -> StopwatchRed
                        else -> TextPrimary
                    }
                )
                if (lap.diffFromAvgMillis != 0L) {
                    val isBetter = lap.diffFromAvgMillis < 0
                    Text(
                        text = TimeFormatter.formatLapDiff(lap.diffFromAvgMillis),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isBetter) StopwatchGreen.copy(alpha = 0.8f) else StopwatchRed.copy(alpha = 0.8f)
                    )
                }
            }

            // Total Elapsed Time
            Text(
                text = TimeFormatter.format(lap.totalTimeMillis),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

private fun buildSummary(laps: List<LapItem>, totalTime: Long): String {
    val sb = StringBuilder()
    sb.append("⏱️ Stopwatch Laps Summary\n")
    sb.append("Total Time: ${TimeFormatter.format(totalTime)}\n")
    sb.append("Total Laps: ${laps.size}\n\n")
    laps.reversed().forEach { lap ->
        val tag = when {
            lap.isFastest -> " [FASTEST]"
            lap.isSlowest -> " [SLOWEST]"
            else -> ""
        }
        sb.append(String.format("Lap %02d: %s (Total: %s)%s\n",
            lap.lapNumber,
            TimeFormatter.format(lap.lapTimeMillis),
            TimeFormatter.format(lap.totalTimeMillis),
            tag
        ))
    }
    return sb.toString()
}

private fun shareLaps(context: Context, summary: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, summary)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Stopwatch Laps")
    context.startActivity(shareIntent)
}
