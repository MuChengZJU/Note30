package com.example.note30

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.UUID
import java.util.concurrent.TimeUnit

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val FOLLOW_UP_TAG_KEY = "follow_up_tag"
        const val WORK_NAME = "note30_reminder_work"
    }

    override suspend fun doWork(): Result {
        val followUpTag = "follow_up_${UUID.randomUUID()}"
        scheduleFollowUpWork(followUpTag)
        sendNotification(followUpTag)
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