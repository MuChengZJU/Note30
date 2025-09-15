package com.example.inspirationmushroom

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class RecordStatus {
    PENDING_ANALYSIS,
    ANALYZED,
    ANALYSIS_FAILED
}

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Date = Date(),
    val content: String,
    val aiAnalysis: String? = null, // 存储 AI 返回的 JSON
    val status: RecordStatus = RecordStatus.PENDING_ANALYSIS
)