package com.example.note30

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecordScreen(navController: NavController, viewModel: RecordViewModel) {
    var text by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf(3) } // Default to 3
    var mood by remember { mutableStateOf<String?>(null) }
    val isPaused by viewModel.isPaused.collectAsState()
    var showSummaryDialog by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.showSummaryDialog.collect { startTime ->
            showSummaryDialog = startTime
        }
    }

    if (showSummaryDialog != null) {
        SummaryDialog(
            pauseStartTime = showSummaryDialog!!,
            onDismiss = { showSummaryDialog = null },
            onSave = { content, efficiency, mood ->
                viewModel.saveRecord(
                    content = content,
                    efficiency = efficiency,
                    mood = mood,
                    timestamp = Date(showSummaryDialog!!)
                )
                viewModel.confirmResumeReminders()
                showSummaryDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "记录", 
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 8.dp,
                actions = {
                    IconButton(onClick = {
                        if (isPaused) viewModel.resumeReminders() else viewModel.pauseReminders()
                    }) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "恢复提醒" else "暂停提醒",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        },
        backgroundColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            if (isPaused) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(12.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = "暂停",
                            tint = Color(0xFFEF6C00)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "提醒已暂停",
                            style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFEF6C00)
                        )
                    }
                }
            }

            // Text Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "记录内容",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF6200EA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = { Text("在这里记录你刚才做了什么...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF6200EA),
                            cursorColor = Color(0xFF6200EA),
                            backgroundColor = Color(0xFFFAFAFA)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )
                }
            }

            // Efficiency Rating Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "效率评级",
                            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFF6200EA)
                        )
                        Surface(
                            color = when (efficiency) {
                                1, 2 -> Color(0xFFFFEBEE)
                                3 -> Color(0xFFFFF8E1)
                                4, 5 -> Color(0xFFE8F5E8)
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "$efficiency",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                                color = when (efficiency) {
                                    1, 2 -> Color(0xFFE53935)
                                    3 -> Color(0xFFFF8F00)
                                    4, 5 -> Color(0xFF43A047)
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 效率级别描述
                    val efficiencyDesc = when (efficiency) {
                        1 -> "很低效"
                        2 -> "低效"
                        3 -> "一般"
                        4 -> "高效"
                        5 -> "非常高效"
                        else -> "一般"
                    }
                    Text(
                        text = efficiencyDesc,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1", style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium))
                        Slider(
                            value = efficiency.toFloat(),
                            onValueChange = { newValue -> 
                                efficiency = newValue.toInt().coerceIn(1, 5)
                            },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6200EA),
                                activeTrackColor = Color(0xFF6200EA),
                                inactiveTrackColor = Color(0xFF6200EA).copy(alpha = 0.24f)
                            )
                        )
                        Text("5", style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }

            // Mood Selection Card
            var expanded by remember { mutableStateOf(false) }
            val moods = listOf("平静", "高效", "疲惫", "分心", "愉悦", "焦虑")
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "情绪标签",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF6200EA)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (mood != null) Color(0xFF6200EA) 
                                             else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                backgroundColor = if (mood != null) Color(0xFF6200EA).copy(alpha = 0.08f)
                                                 else Color.Transparent
                            )
                        ) {
                            Text(
                                mood ?: "选择情绪", 
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.body1
                            )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6200EA),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "保存",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "保存记录",
                    style = MaterialTheme.typography.button.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun SummaryDialog(
    pauseStartTime: Long,
    onDismiss: () -> Unit,
    onSave: (String, Int, String?) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var efficiency by remember { mutableStateOf(3) }
    var mood by remember { mutableStateOf<String?>(null) }
    val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
    val startTimeString = dateFormat.format(Date(pauseStartTime))
    val endTimeString = dateFormat.format(Date())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "暂停时段总结",
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "你从 $startTimeString 到 $endTimeString 暂停了提醒，请总结一下这段时间做了什么。",
                    style = MaterialTheme.typography.body2
                )
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("总结内容") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Simplified Efficiency & Mood pickers can be added here if needed
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("跳过")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(text, efficiency, mood) }) {
                        Text("保存总结")
                    }
                }
            }
        }
    }
}