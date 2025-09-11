package com.example.note30

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordScreen(navController: NavController, viewModel: RecordViewModel, reminderManager: ReminderManager) {
    var text by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf(3) } // Default to 3
    var mood by remember { mutableStateOf<String?>(null) }
    var isPaused by remember { mutableStateOf(false) }
    var pauseStartTime by remember { mutableStateOf<Long?>(null) }
    var showSummaryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("记录界面", style = MaterialTheme.typography.h5)

        // Text Input
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("记录内容") },
            modifier = Modifier.fillMaxWidth()
        )

        // Efficiency Rating
        Text("效率评级: $efficiency")
        Slider(
            value = efficiency.toFloat(),
            onValueChange = { efficiency = it.toInt() },
            valueRange = 1f..5f,
            steps = 3, // Creates 4 intervals (1-2, 2-3, 3-4, 4-5)
            modifier = Modifier.fillMaxWidth()
        )

        // Mood Selection (Simple dropdown for now)
        var expanded by remember { mutableStateOf(false) }
        val moods = listOf("平静", "高效", "疲惫", "分心", "愉悦", "焦虑")
        Text("情绪标签:")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = mood ?: "选择情绪",
                onValueChange = { },
                label = { Text("情绪") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                moods.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            mood = item
                            expanded = false
                        }
                    ) {
                        Text(text = item)
                    }
                }
                // Option to clear selection
                DropdownMenuItem(
                    onClick = {
                        mood = null
                        expanded = false
                    }
                ) {
                    Text(text = "无")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Pause/Resume Button
        Button(
            onClick = {
                if (isPaused) {
                    // Resume logic
                    pauseStartTime?.let { startTime ->
                        // Show summary dialog
                        showSummaryDialog = true
                    }
                    isPaused = false
                    pauseStartTime = null
                } else {
                    // Pause logic
                    reminderManager.stopReminders()
                    isPaused = true
                    pauseStartTime = System.currentTimeMillis()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isPaused) "恢复" else "暂停")
        }

        // Save Button
        Button(
            onClick = {
                viewModel.saveRecord(text, efficiency, mood)
                // Reset form after saving
                text = ""
                efficiency = 3
                mood = null
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("保存")
        }
    }

    // Summary Dialog
    if (showSummaryDialog) {
        SummaryDialog(
            onDismiss = { showSummaryDialog = false },
            onConfirm = { summaryText ->
                // Save summary record
                viewModel.saveRecord(summaryText, 3, null) // Default efficiency and no mood for summary
                reminderManager.startPeriodicReminders()
                showSummaryDialog = false
            },
            pauseStartTime = pauseStartTime ?: System.currentTimeMillis()
        )
    }
}

@Composable
fun SummaryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    pauseStartTime: Long
) {
    var summaryText by remember { mutableStateOf("") }
    val pauseDuration = System.currentTimeMillis() - pauseStartTime
    val formattedDuration = formatDuration(pauseDuration)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("暂停总结") },
        text = {
            Column {
                Text("您已暂停 $formattedDuration，请总结这段时间的工作内容：")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = summaryText,
                    onValueChange = { summaryText = it },
                    label = { Text("总结内容") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(summaryText) },
                enabled = summaryText.isNotBlank()
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun formatDuration(durationMillis: Long): String {
    val seconds = durationMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return if (hours > 0) {
        "${hours}小时${minutes % 60}分钟"
    } else {
        "${minutes}分钟"
    }
}