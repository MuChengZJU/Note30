package com.example.note30

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Pause

@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = viewModel()) {
    val records by viewModel.getAllRecords().collectAsState(initial = emptyList())
    val isPaused by viewModel.isPaused.collectAsState()
    var viewMode by remember { mutableStateOf("Timeline") } // "Timeline" or "DateList"
    var showExportDialog by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.exportedMarkdown.collect { markdown ->
            if (markdown.isNotBlank()) {
                // Show toast
                Toast.makeText(context, "记录已生成", Toast.LENGTH_SHORT).show()

                // Copy to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Note30 Export", markdown)
                clipboard.setPrimaryClip(clip)

                // Share intent
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, markdown)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "分享您的记录")
                context.startActivity(shareIntent)
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出记录") },
            text = { Text("将今天的所有记录生成 Markdown 文本，并复制到剪贴板和通过系统分享。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.exportTodaysRecords()
                    showExportDialog = false
                }) {
                    Text("导出今天")
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = {
                        showExportDialog = false
                        showDateRangePicker = true
                    }) {
                        Text("选择日期范围")
                    }
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    if (showDateRangePicker) {
        Dialog(onDismissRequest = { showDateRangePicker = false }) {
            Surface(shape = MaterialTheme.shapes.medium) {
                DateRangePicker(
                    onDateRangeSelected = { start, end ->
                        viewModel.exportRecordsByDateRange(start, end)
                        showDateRangePicker = false
                    },
                    onDismiss = { showDateRangePicker = false }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "历史记录", 
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 8.dp,
                actions = {
                    IconButton(onClick = {
                        viewMode = if (viewMode == "Timeline") "DateList" else "Timeline"
                    }) {
                        Icon(
                            Icons.Default.ViewList, 
                            contentDescription = "切换视图",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            Icons.Default.Share, 
                            contentDescription = "导出",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            )
        },
        backgroundColor = Color(0xFFF8F9FF)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (isPaused) {
                PauseBanner()
            }

            // Conditional view rendering
            when (viewMode) {
                "Timeline" -> TimelineView(records)
                "DateList" -> DateListView(records) { /* TODO: Navigate to single day view */ }
            }
        }
    }
}

@Composable
fun TimelineView(records: List<Record>) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "无记录",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "还没有任何记录",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(records) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "🕰️",
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(record.timestamp),
                                style = MaterialTheme.typography.h6.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF667EEA)
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = when (record.efficiency) {
                                            1, 2 -> listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E))
                                            3 -> listOf(Color(0xFFFFE66D), Color(0xFFFFF176))
                                            4, 5 -> listOf(Color(0xFF4ECDC4), Color(0xFF44E5E7))
                                            else -> listOf(Color.Gray, Color.LightGray)
                                        }
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (record.efficiency) {
                                        1, 2 -> "😔"
                                        3 -> "😐"
                                        4, 5 -> "🚀"
                                        else -> "🤔"
                                    },
                                    style = MaterialTheme.typography.body2
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${record.efficiency}",
                                    style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    record.mood?.let { mood ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF667EEA).copy(alpha = 0.2f),
                                            Color(0xFF764BA2).copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF667EEA)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        record.content,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DateListView(records: List<Record>, onDateClick: (String) -> Unit) {
    val groupedRecords = records.groupBy {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.timestamp)
    }

    if (groupedRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "无记录",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "还没有任何记录",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(groupedRecords.keys.sortedDescending()) { date ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateClick(date) },
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "📅",
                            style = MaterialTheme.typography.h6
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = date, 
                                style = MaterialTheme.typography.h6.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF667EEA)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${groupedRecords[date]?.size ?: 0} 条记录", 
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                ),
                                shape = RoundedCornerShape(25.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${groupedRecords[date]?.size ?: 0}",
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}