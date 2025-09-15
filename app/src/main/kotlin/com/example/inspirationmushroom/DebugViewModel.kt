package com.example.inspirationmushroom

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DebugViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    private val _reminderWorkInfo = MutableStateFlow<WorkInfo?>(null)
    val reminderWorkInfo: StateFlow<WorkInfo?> = _reminderWorkInfo.asStateFlow()

    private val _nextReminderTime = MutableStateFlow<Long?>(null)
    val nextReminderTime: StateFlow<Long?> = _nextReminderTime.asStateFlow()

    init {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData(ReminderWorker.WORK_NAME)
                .asFlow()
                .map { it.firstOrNull() }
                .collect { workInfo ->
                    _reminderWorkInfo.value = workInfo
                    if (workInfo?.state == WorkInfo.State.ENQUEUED) {
                        _nextReminderTime.value = workInfo.nextScheduleTimeMillis
                    } else {
                        _nextReminderTime.value = null
                    }
                }
        }
    }

    fun triggerReminderNow() {
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        workManager.enqueue(oneTimeWorkRequest)
    }
}
