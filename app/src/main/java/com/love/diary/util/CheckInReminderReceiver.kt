package com.love.diary.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver for handling check-in reminder alarms
 * Triggered by AlarmManager at scheduled time for individual check-in items
 */
class CheckInReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CheckInReminderReceiver"
        const val EXTRA_CHECK_IN_CONFIG_ID = "extra_check_in_config_id"
        const val EXTRA_CHECK_IN_NAME = "extra_check_in_name"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val checkInConfigId = intent.getLongExtra(EXTRA_CHECK_IN_CONFIG_ID, -1L)
        val checkInName = intent.getStringExtra(EXTRA_CHECK_IN_NAME) ?: "打卡"
        
        Log.d(TAG, "Check-in reminder triggered for: $checkInName (ID: $checkInConfigId)")
        
        // Show the reminder notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showCheckInReminder(
            checkInConfigId = checkInConfigId,
            title = "打卡提醒",
            message = "该打卡了：$checkInName"
        )
        
        // For Android 12+, reschedule for next day since we can't use setRepeating with exact alarms
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val hour = intent.getIntExtra(EXTRA_HOUR, 9)
            val minute = intent.getIntExtra(EXTRA_MINUTE, 0)
            
            // Reschedule for next day
            val scheduler = CheckInReminderScheduler(context)
            scheduler.scheduleCheckInReminder(checkInConfigId, checkInName, hour, minute)
            Log.d(TAG, "Rescheduled check-in reminder for next day at $hour:${minute.toString().padStart(2, '0')}")
        }
    }
}
