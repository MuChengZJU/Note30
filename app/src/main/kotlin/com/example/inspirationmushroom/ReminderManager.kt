package com.example.inspirationmushroom

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class ReminderManager(private val context: Context) {
    
    fun startPeriodicReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
            
        val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            reminderWork
        )
    }
    
    fun stopReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }
}