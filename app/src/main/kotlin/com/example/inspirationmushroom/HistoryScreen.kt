package com.example.inspirationmushroom

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inspirationmushroom.ai.AnalysisResult
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error

@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = viewModel()) {
    val records by viewModel.getAllRecords().collectAsState(initial = emptyList())
    val isPaused by viewModel.isPaused.collectAsState()
    var viewMode by remember { mutableStateOf("Timeline") } // "Timeline" or "DateList"
    var showExportDialog by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.exportedMarkdown.collect { markdown ->
            if (markdown.isNotBlank()) {
                // Show toast
                Toast.makeText(context, "记录已生成", Toast.LENGTH_SHORT).show()

                // Copy to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Note30 Export", markdown)
                clipboard.setPrimaryClip(clip)
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
                "Timeline" -> TimelineView(records) { rec ->
                    scope.launch { viewModel.retryAnalysis(rec) }
                }
                "DateList" -> DateListView(records) { /* TODO: Navigate to single day view */ }
            }
        }
    }
}

@Composable
fun TimelineView(records: List<Record>, onRetry: (Record) -> Unit) {
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
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(records) { record ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // 时间和状态栏
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

                        // 分析状态指示器
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (record.status) {
                                RecordStatus.PENDING_ANALYSIS -> {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = "分析中",
                                        tint = Color(0xFFFFA000)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("分析中", color = Color(0xFFFFA000))
                                    IconButton(onClick = { onRetry(record) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "重试分析", tint = MaterialTheme.colors.primary)
                                    }
                                }
                                RecordStatus.ANALYZED -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "已分析",
                                        tint = Color(0xFF388E3C)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("已分析", color = Color(0xFF388E3C))
                                }
                                RecordStatus.ANALYSIS_FAILED -> {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "分析失败",
                                        tint = Color(0xFFD32F2F)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("分析失败", color = Color(0xFFD32F2F))
                                    IconButton(onClick = { onRetry(record) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "重试分析", tint = Color(0xFFF44336))
                                    }
                                }
                            }
                        }
                    }

                    // 原文
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "原文",
                        style = MaterialTheme.typography.subtitle2.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        record.content,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )

                    // AI分析结果
                    record.aiAnalysis?.let { analysisJson ->
                        val analysisResult = AnalysisResult.fromJson(analysisJson)
                        analysisResult?.let { result ->
                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color(0xFFF8F9FF),
                                elevation = 2.dp,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "🤖 AI 分析",
                                        style = MaterialTheme.typography.subtitle2.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colors.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 情绪
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("😊", style = MaterialTheme.typography.body2)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "情绪: ${result.emotion}",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 关键词
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text("🏷️", style = MaterialTheme.typography.body2)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "关键词: ${result.keywords.joinToString(", ")}",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 摘要
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text("📝", style = MaterialTheme.typography.body2)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "摘要: ${result.summary}",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 分类
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📂", style = MaterialTheme.typography.body2)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "分类: ${result.category}",
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                }
                            }
                        }
                    }
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