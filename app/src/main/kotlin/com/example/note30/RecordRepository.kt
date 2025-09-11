package com.example.note30

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecordRepository(private val recordDao: RecordDao) {

    fun getAllRecords() = recordDao.getAllRecords()
    
    fun getRecordsByDateRange(startTime: Long, endTime: Long) = recordDao.getRecordsByDateRange(startTime, endTime)

    suspend fun insert(record: Record) {
        recordDao.insert(record)
    }

    suspend fun update(record: Record) {
        recordDao.update(record)
    }

    suspend fun delete(record: Record) {
        recordDao.delete(record)
    }
}