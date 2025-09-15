package com.example.inspirationmushroom

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class RecordViewModel(application: Application, private val repository: RecordRepository) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application.applicationContext)
    private val sharedPreferences = application.getSharedPreferences("note30_prefs", Context.MODE_PRIVATE)

    private val _isPaused = MutableStateFlow(sharedPreferences.getBoolean("isPaused", false))
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _recordSaved = MutableSharedFlow<Unit>()
    val recordSaved: SharedFlow<Unit> = _recordSaved
    
    private val _showSummaryDialog = MutableSharedFlow<Long>()
    val showSummaryDialog: SharedFlow<Long> = _showSummaryDialog

    fun saveRecord(content: String, timestamp: Date = Date()) {
        viewModelScope.launch {
            val record = Record(
                timestamp = timestamp,
                content = content,
                status = RecordStatus.PENDING_ANALYSIS
            )
            repository.saveRecordAndTriggerAnalysis(record)
            _recordSaved.emit(Unit)
        }
    }

    fun pauseReminders() {
        workManager.cancelUniqueWork("note30_reminder_work")
        with(sharedPreferences.edit()) {
            putBoolean("isPaused", true)
            putLong("pause_start_timestamp", System.currentTimeMillis())
            apply()
        }
        _isPaused.value = true
    }

    fun resumeReminders() {
        viewModelScope.launch {
            val pauseStartTime = sharedPreferences.getLong("pause_start_timestamp", 0L)
            if (pauseStartTime > 0) {
                _showSummaryDialog.emit(pauseStartTime)
            } else {
                // If for some reason startTime is not available, resume directly
                confirmResumeReminders()
            }
        }
    }
    
    fun confirmResumeReminders() {
        with(sharedPreferences.edit()) {
            putBoolean("isPaused", false)
            remove("pause_start_timestamp")
            apply()
        }
        _isPaused.value = false

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(
            "note30_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}