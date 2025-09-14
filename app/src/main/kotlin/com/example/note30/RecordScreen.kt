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
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.clickable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.lazy.LazyColumn

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
        backgroundColor = Color(0xFFF8F9FF)
    ) { paddingValues ->
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp) // 顶部和底部留出额外空间
        ) {
            if (isPaused) {
                item {
                    PauseBanner()
                }
            }

            item {
                // Text Input Card 📝
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "📝",
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "记录内容",
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF667EEA)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
                        placeholder = { Text("在这里记录你刚才做了什么...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF667EEA),
                            cursorColor = Color(0xFF667EEA),
                            backgroundColor = Color(0xFFF8F9FF)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 6
                    )
                }
            }
            }

            item {
                // Efficiency Rating Card ⚡
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "⚡",
                                    style = MaterialTheme.typography.h6
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "效率评级",
                                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF667EEA)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = when (efficiency) {
                                                1, 2 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
                                                3 -> listOf(Color(0xFFFFE66D), Color(0xFFFFF176))
                                                4, 5 -> listOf(Color(0xFF4ECDC4), Color(0xFF44E5E7))
                                                else -> listOf(Color.Gray, Color.LightGray)
                                            }
                                        ),
                                        shape = RoundedCornerShape(25.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "$efficiency",
                                    style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { level ->
                                    val selected = efficiency == level
                                    val bg = if (selected) Brush.horizontalGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))) else null
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.Transparent,
                                        border = if (selected) null else ButtonDefaults.outlinedBorder,
                                        elevation = if (selected) 4.dp else 0.dp
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .height(36.dp)
                                                .width(48.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .then(
                                                    if (selected) {
                                                        Modifier.background(
                                                            brush = Brush.horizontalGradient(
                                                                listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                                            )
                                                        )
                                                    } else {
                                                        Modifier.background(color = Color.Transparent)
                                                    }
                                                )
                                                .clickable(
                                                    role = Role.Button
                                                ) { efficiency = level },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = level.toString(),
                                                color = if (selected) Color.White else MaterialTheme.colors.onSurface,
                                                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Mood Selection Card 🌈
        var expanded by remember { mutableStateOf(false) }
                val moods = listOf("😌 平静", "🔥 高效", "😴 疲惫", "😵‍💫 分心", "😊 愉悦", "😰 焦虑")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "🌈",
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "情绪标签",
                                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF667EEA)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (mood != null) Color(0xFF667EEA)
                                            else MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    backgroundColor = if (mood != null) Color(0xFF667EEA).copy(alpha = 0.08f)
                                            else Color.Transparent
                                ),
                                border = if (mood != null) ButtonDefaults.outlinedBorder.copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                    )
                                ) else ButtonDefaults.outlinedBorder
                            ) {
                                Text(
                                    mood ?: "🙂 选择情绪",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
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
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                // Save Button 🚀 (自绘渐变+点击)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                            )
                        )
                        .clickable(
                            role = Role.Button
                        ) {
                viewModel.saveRecord(text, efficiency, mood)
                text = ""
                efficiency = 3
                mood = null
            },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🚀", style = MaterialTheme.typography.h6, color = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("保存记录", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
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