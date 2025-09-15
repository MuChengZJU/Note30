package com.example.inspirationmushroom

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    
    fun startPeriodicReminders() {
        scheduleWithInterval(30)
    }

    fun restartWithInterval(minutes: Int) {
        // Cancel existing work and schedule with new interval
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
        scheduleWithInterval(minutes)
    }

    private fun scheduleWithInterval(minutes: Int) {
        val safeMinutes = minutes.coerceAtLeast(15) // WorkManager minimum is 15 minutes

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
            
        val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(safeMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWork
        )
    }
    
    fun stopReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }
}