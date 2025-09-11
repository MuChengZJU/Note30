package com.example.note30

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(private val repository: RecordRepository) : ViewModel() {

    fun getAllRecords(): Flow<List<Record>> {
        return repository.getAllRecords()
    }

    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<Record>> {
        return repository.getRecordsByDateRange(startTime, endTime)
    }

    fun exportToMarkdown(records: List<Record>): String {
        if (records.isEmpty()) return ""

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
}