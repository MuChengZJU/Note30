package com.example.inspirationmushroom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.inspirationmushroom.InspirationMushroomApplication

class FollowUpWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Create an Intent to launch MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val sharedPrefs = applicationContext.getSharedPreferences("Note30Prefs", Context.MODE_PRIVATE)
        val reminderType = sharedPrefs.getString("reminder_type", "vibration")

        // Build the notification
        val builder = NotificationCompat.Builder(applicationContext, InspirationMushroomApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle("还在忙吗？")
            .setContentText("别忘了记录下你的工作和心情哦！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Sound and vibration are controlled by the notification channel on API 26+
        // Relying on the channel configuration set in Note30Application.

        // Show the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(2, builder.build())
        }

        return Result.success()
    }
}
