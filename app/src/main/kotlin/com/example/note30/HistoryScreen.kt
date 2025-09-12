package com.example.note30

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        backgroundColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(records) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(record.timestamp),
                            style = MaterialTheme.typography.subtitle1.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EA)
                            )
                        )
                        Surface(
                            color = when (record.efficiency) {
                                1, 2 -> Color(0xFFFFCDD2)
                                3 -> Color(0xFFFFF9C4)
                                4, 5 -> Color(0xFFC8E6C9)
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "效率: ${record.efficiency}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                                color = when (record.efficiency) {
                                    1, 2 -> Color(0xFFD32F2F)
                                    3 -> Color(0xFFF57C00)
                                    4, 5 -> Color(0xFF388E3C)
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                    
                    record.mood?.let { mood ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF6200EA).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "😊 $mood",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.caption,
                                color = Color(0xFF6200EA)
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(groupedRecords.keys.sortedDescending()) { date ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateClick(date) },
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = date, 
                            style = MaterialTheme.typography.h6.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EA)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${groupedRecords[date]?.size ?: 0} 条记录", 
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Surface(
                        color = Color(0xFF6200EA).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "${groupedRecords[date]?.size ?: 0}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.subtitle2.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF6200EA)
                        )
                    }
                }
            }
        }
    }
}