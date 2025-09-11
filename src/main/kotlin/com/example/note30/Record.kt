package com.example.note30

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "records")
data class Record(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val timestamp: Date = Date(),
    val efficiency: Int, // 1-5
    val mood: String?, // Optional
    val content: String
)