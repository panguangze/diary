package com.love.diary.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.love.diary.MainActivity
import com.love.diary.R

/**
 * Helper class for managing app notifications
 * Handles reminder notifications for mood tracking
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "diary_reminders"
        private const val CHANNEL_NAME = "日记提醒"
        private const val CHANNEL_DESCRIPTION = "每日心情记录提醒"
        const val NOTIFICATION_ID_DAILY_REMINDER = 1001
    }

    init {
        createNotificationChannel()
    }

    /**
     * Create notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show daily reminder notification
     * @param title Notification title
     * @param message Notification message
     * 
     * Note: The icon should be replaced with app-specific icon (R.drawable.ic_notification)
     * when the app icon resource is available
     */
    fun showDailyReminder(
        title: String = "记录今天的心情",
        message: String = "今天对我们的关系有什么感受吗？"
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // TODO: Replace with app icon when available: R.drawable.ic_notification
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_DAILY_REMINDER,
                notification
            )
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            android.util.Log.w("NotificationHelper", "Notification permission not granted", e)
        }
    }

    /**
     * Show anniversary notification
     * @param dayCount Number of days together
     * 
     * Note: The icon should be replaced with app-specific icon (R.drawable.ic_notification)
     * when the app icon resource is available
     */
    fun showAnniversaryReminder(dayCount: Int) {
        val title = "纪念日提醒"
        val message = "🎉 今天是我们在一起的第 $dayCount 天！"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // TODO: Replace with app icon when available: R.drawable.ic_notification
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message + "\n感谢你一直以来的陪伴。")
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_DAILY_REMINDER + 1,
                notification
            )
        } catch (e: SecurityException) {
            android.util.Log.w("NotificationHelper", "Notification permission not granted", e)
        }
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Check if notifications are enabled
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
