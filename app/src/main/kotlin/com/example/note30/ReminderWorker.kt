package com.example.note30

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.UUID
import java.util.concurrent.TimeUnit
import android.content.SharedPreferences

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val FOLLOW_UP_TAG_KEY = "follow_up_tag"
        const val WORK_NAME = "note30_reminder_work"
    }

    override suspend fun doWork(): Result {
        val followUpWorkTag = "followUp_${System.currentTimeMillis()}"

        // Create an Intent to launch MainActivity
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("cancel_follow_up_work_tag", followUpWorkTag)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        
        val sharedPrefs = applicationContext.getSharedPreferences("Note30Prefs", Context.MODE_PRIVATE)
        val reminderType = sharedPrefs.getString("reminder_type", "vibration")

        // Build the notification
        val builder = NotificationCompat.Builder(applicationContext, Note30Application.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setContentTitle("过了30分钟了")
            .setContentText("记录一下刚才做了什么，感受如何？")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setDefaults(NotificationCompat.DEFAULT_ALL)


        // On Android 8.0 (API 26) and higher, sound and vibration are controlled by the notification channel.
        // The .setDefaults call is primarily for older versions.
        // By setting them on the channel, we ensure consistent behavior.
        // We are removing the explicit call here to rely on the channel's configuration.

        // Show the notification
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(1, builder.build())
        }

        // Schedule a follow-up worker
        scheduleFollowUpWork(followUpWorkTag)
        return Result.success()
    }

    private fun scheduleFollowUpWork(tag: String) {
        val followUpWorkRequest = OneTimeWorkRequestBuilder<FollowUpWorker>()
            .setInitialDelay(3, TimeUnit.MINUTES)
            .addTag(tag)
            .build()
        WorkManager.getInstance(context).enqueue(followUpWorkRequest)
    }

    private fun sendNotification(followUpTag: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "note30_reminder_channel"

        // Create a notification channel for Android 8.0+
        val channel = NotificationChannel(
            channelId,
            "Note30 Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for 30-minute interval reminders"
        }
        notificationManager.createNotificationChannel(channel)

        // Intent to open MainActivity when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(FOLLOW_UP_TAG_KEY, followUpTag)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder icon
            .setContentTitle("30分钟过去了")
            .setContentText("记录一下你刚才做了什么，感觉如何？")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}