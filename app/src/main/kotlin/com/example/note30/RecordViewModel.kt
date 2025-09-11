package com.example.note30

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

    fun saveRecord(content: String, efficiency: Int, mood: String?) {
        viewModelScope.launch {
            val record = Record(
                timestamp = Date(),
                efficiency = efficiency,
                mood = mood,
                content = content
            )
            repository.insert(record)
            _recordSaved.emit(Unit)
        }
    }

    fun pauseReminders() {
        workManager.cancelUniqueWork("note30_reminder_work")
        sharedPreferences.edit().putBoolean("isPaused", true).apply()
        _isPaused.value = true
    }

    fun resumeReminders() {
        sharedPreferences.edit().putBoolean("isPaused", false).apply()
        _isPaused.value = false
        
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(
            "note30_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}