package com.example.inspirationmushroom

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspirationmushroom.ai.AnalysisResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(
    application: Application,
    private val repository: RecordRepository
) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("note30_prefs", Context.MODE_PRIVATE)

    private val _isPaused = MutableStateFlow(sharedPreferences.getBoolean("isPaused", false))
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _exportedMarkdown = MutableSharedFlow<String>()
    val exportedMarkdown: SharedFlow<String> = _exportedMarkdown

    fun getAllRecords() = repository.getAllRecords()

    val records = repository.getAllRecords().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<Record>> {
        return repository.getRecordsByDateRange(startTime, endTime)
    }

    fun exportTodaysRecords() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endTime = calendar.timeInMillis

            val todaysRecords = repository.getRecordsByDateRange(startTime, endTime).first()
            val markdown = exportToMarkdown(todaysRecords)
            _exportedMarkdown.emit(markdown)
        }
    }

    fun exportRecordsByDateRange(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            // Ensure endTime is the end of the selected day
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = endTime
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val adjustedEndTime = calendar.timeInMillis

            val records = repository.getRecordsByDateRange(startTime, adjustedEndTime).first()
            val markdown = exportToMarkdown(records)
            _exportedMarkdown.emit(markdown)
        }
    }

    fun exportToMarkdown(records: List<Record>): String {
        if (records.isEmpty()) return "选定范围内没有任何记录。"

        // Group records by date
        val groupedRecords = records.groupBy { record ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(record.timestamp)
        }

        val markdownBuilder = StringBuilder()

        groupedRecords.forEach { (date, dailyRecords) ->
            markdownBuilder.append("# $date\n\n")

            // Sort records by timestamp for chronological order within a day
            dailyRecords.sortedBy { it.timestamp }.forEach { record ->
                val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val startTime = timeFormatter.format(record.timestamp)

                markdownBuilder.append("## $startTime\n")
                markdownBuilder.append("- 原文: ${record.content}\n")

                // 解析并展示AI分析结果
                record.aiAnalysis?.let { analysisJson ->
                    val analysisResult = AnalysisResult.fromJson(analysisJson)
                    analysisResult?.let { result ->
                        markdownBuilder.append("- AI 分析:\n")
                        markdownBuilder.append("  - 情绪: ${result.emotion}\n")
                        markdownBuilder.append("  - 关键词: ${result.keywords.joinToString(", ")}\n")
                        markdownBuilder.append("  - 摘要: ${result.summary}\n")
                        markdownBuilder.append("  - 分类: ${result.category}\n")
                    }
                }

                // 显示分析状态（如果分析失败）
                if (record.status == RecordStatus.ANALYSIS_FAILED) {
                    markdownBuilder.append("- 分析状态: 分析失败\n")
                }

                markdownBuilder.append("\n")
            }
        }

        return markdownBuilder.toString()
    }

    fun retryAnalysis(record: Record) {
        viewModelScope.launch {
            repository.retryAnalysis(record)
        }
    }

    // For testing export functionality
    fun testExport(): String {
        val testRecords = listOf(
            Record(
                timestamp = Date(),
                content = "完成了项目 A 的需求文档初稿，感觉很有成就感。",
                aiAnalysis = """{"emotion":"积极","keywords":["项目A","文档","成就感"],"summary":"用户完成了工作任务并感到满意","category":"工作"}""",
                status = RecordStatus.ANALYZED
            ),
            Record(
                timestamp = Date(System.currentTimeMillis() + 30 * 60 * 1000),
                content = "参加了团队的每日站会，讨论了一些问题，进行得很顺利。",
                aiAnalysis = """{"emotion":"平静","keywords":["站会","团队","讨论"],"summary":"参加了例行的团队会议","category":"工作"}""",
                status = RecordStatus.ANALYZED
            )
        )

        return exportToMarkdown(testRecords)
    }
}