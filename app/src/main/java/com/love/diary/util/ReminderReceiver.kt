package com.love.diary.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver for handling daily reminder alarms
 * Triggered by AlarmManager at scheduled time
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ReminderReceiver"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Daily reminder triggered")
        
        // Show the reminder notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showDailyReminder(
            title = "记录今天的心情",
            message = "今天对我们的关系有什么感受吗？"
        )
        
        // For Android 12+, reschedule for next day since we can't use setRepeating with exact alarms
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val hour = intent.getIntExtra(EXTRA_HOUR, 9)
            val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
            
            // Reschedule for next day
            val scheduler = ReminderScheduler(context)
            scheduler.scheduleDailyReminder(hour, minute)
            Log.d(TAG, "Rescheduled reminder for next day at $hour:${minute.toString().padStart(2, '0')}")
        }
    }
}
