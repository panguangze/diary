package com.love.diary.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

/**
 * Helper class for scheduling daily reminder notifications
 * Uses AlarmManager to schedule repeating alarms at a specific time each day
 */
class ReminderScheduler(private val context: Context) {

    companion object {
        private const val TAG = "ReminderScheduler"
        private const val REMINDER_REQUEST_CODE = 1001
    }

    /**
     * Schedule daily reminder at specified time
     * @param hourOfDay Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleDailyReminder(hourOfDay: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set the alarm to start at the specified time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            // Use setRepeating for daily alarms
            // Note: On Android 6.0+ (API 23+), inexact alarms may be used to save battery
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            
            Log.d(TAG, "Daily reminder scheduled for ${hourOfDay}:${minute.toString().padStart(2, '0')}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder", e)
        }
    }

    /**
     * Schedule daily reminder at specified time in minutes from midnight
     * @param timeInMinutes Time in minutes from midnight (0-1439)
     */
    fun scheduleDailyReminder(timeInMinutes: Int) {
        val hourOfDay = timeInMinutes / 60
        val minute = timeInMinutes % 60
        scheduleDailyReminder(hourOfDay, minute)
    }

    /**
     * Cancel the scheduled daily reminder
     */
    fun cancelDailyReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Daily reminder canceled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel reminder", e)
        }
    }
}
