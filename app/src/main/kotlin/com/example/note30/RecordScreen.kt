package com.example.note30

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecordScreen(navController: NavController, viewModel: RecordViewModel) {
    var text by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf(3) } // Default to 3
    var mood by remember { mutableStateOf<String?>(null) }
    val isPaused by viewModel.isPaused.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录") },
                actions = {
                    IconButton(onClick = {
                        if (isPaused) viewModel.resumeReminders() else viewModel.pauseReminders()
                    }) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "恢复提醒" else "暂停提醒"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            // Mood Selection using standard DropdownMenu
            var expanded by remember { mutableStateOf(false) }
            val moods = listOf("平静", "高效", "疲惫", "分心", "愉悦", "焦虑")
            Text("情绪标签:")
            Box {
                Button(onClick = { expanded = true }) {
                    Text(mood ?: "选择情绪")
                }
                DropdownMenu(
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
    }
}