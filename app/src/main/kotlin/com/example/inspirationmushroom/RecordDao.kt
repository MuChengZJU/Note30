package com.example.inspirationmushroom

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE status = :status ORDER BY timestamp DESC")
    fun getRecordsByStatus(status: String): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE status = 'PENDING_ANALYSIS' ORDER BY timestamp DESC")
    fun getPendingAnalysisRecords(): Flow<List<Record>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getRecordById(id: Long): Record?

    @Insert
    suspend fun insert(record: Record): Long

    @Update
    suspend fun update(record: Record)

    @Delete
    suspend fun delete(record: Record)
}