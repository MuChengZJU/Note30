package com.example.note30

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(private val repository: RecordRepository) : ViewModel() {

    private val _exportedMarkdown = MutableSharedFlow<String>()
    val exportedMarkdown: SharedFlow<String> = _exportedMarkdown

    fun getAllRecords(): Flow<List<Record>> {
        return repository.getAllRecords()
    }

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
                // Assuming 30 minute intervals as per PRD
                val endTime = timeFormatter.format(record.timestamp.time + 30 * 60 * 1000)
                
                markdownBuilder.append("## $startTime - $endTime\n")
                markdownBuilder.append("- 效率: ${record.efficiency}\n")
                record.mood?.let { mood ->
                    markdownBuilder.append("- 情绪: $mood\n")
                }
                markdownBuilder.append("- 记录: ${record.content}\n\n")
            }
        }

        return markdownBuilder.toString()
    }
    
    // For testing export functionality
    fun testExport(): String {
        val testRecords = listOf(
            Record(
                id = 1,
                timestamp = Date(),
                efficiency = 4,
                mood = "高效",
                content = "完成了项目 A 的需求文档初稿。"
            ),
            Record(
                id = 2,
                timestamp = Date(System.currentTimeMillis() + 30 * 60 * 1000),
                efficiency = 3,
                mood = null,
                content = "参加了团队的每日站会，讨论了一些问题。"
            )
        )
        
        return exportToMarkdown(testRecords)
    }
}