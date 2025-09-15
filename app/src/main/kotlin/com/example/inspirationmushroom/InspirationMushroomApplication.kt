package com.example.inspirationmushroom

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class InspirationMushroomApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupRecurringWork()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "灵感菇提醒"
            val descriptionText = "灵感菇的AI分析提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH // Necessary for heads-up notifications
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400) // A simple vibration pattern
                val soundUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, null)
                setBypassDnd(true) // Attempt to bypass Do Not Disturb
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(applicationContext)

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        const val CHANNEL_ID = "inspirationmushroom_reminder_channel"
    }
}
