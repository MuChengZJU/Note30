package com.example.note30

import android.app.Application
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class Note30Application : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val sharedPreferences = getSharedPreferences("note30_prefs", Context.MODE_PRIVATE)
        val isPaused = sharedPreferences.getBoolean("isPaused", false)

        if (!isPaused) {
            setupRecurringWork()
        }
    }

    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(applicationContext)

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "note30_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }
}
