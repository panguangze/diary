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
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Daily reminder triggered")
        
        // Show the reminder notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showDailyReminder(
            title = "记录今天的心情",
            message = "今天对我们的关系有什么感受吗？"
        )
    }
}
