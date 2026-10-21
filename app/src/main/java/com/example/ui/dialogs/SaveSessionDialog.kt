package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StopwatchCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.TimeFormatter

@Composable
fun SaveSessionDialog(
    elapsedTimeMillis: Long,
    lapsCount: Int,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Save Session",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Summary preview card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DarkSurfaceBorder.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Total Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = TimeFormatter.format(elapsedTimeMillis),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = StopwatchCyan,
                            fontSize = 16.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Laps", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            text = "$lapsCount laps",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Name") },
                    placeholder = { Text("e.g. 5K Run, Sprint Drills, Pomodoro") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("How was the pace, conditions, etc.") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = StopwatchCyan),
                modifier = Modifier.testTag("confirm_save_session_button")
            ) {
                Text("Save", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_save_session_button")
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
