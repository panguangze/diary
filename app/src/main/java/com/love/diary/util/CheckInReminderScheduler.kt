package com.love.diary.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalTime
import java.util.Calendar

/**
 * Helper class for scheduling check-in reminder notifications
 * Uses AlarmManager to schedule repeating alarms for individual check-in items
 */
class CheckInReminderScheduler(private val context: Context) {

    companion object {
        private const val TAG = "CheckInReminderScheduler"
        private const val REMINDER_REQUEST_CODE_BASE = 2000
    }

    /**
     * Schedule daily reminder for a check-in item
     * @param checkInConfigId The ID of the check-in config
     * @param checkInName The name of the check-in item
     * @param reminderTime Time in HH:mm format (e.g., "09:00")
     */
    fun scheduleCheckInReminder(checkInConfigId: Long, checkInName: String, reminderTime: String) {
        try {
            // Parse time from HH:mm format
            val timeParts = reminderTime.split(":")
            if (timeParts.size != 2) {
                Log.e(TAG, "Invalid time format: $reminderTime")
                return
            }
            
            val hourOfDay = timeParts[0].toIntOrNull() ?: return
            val minute = timeParts[1].toIntOrNull() ?: return
            
            if (hourOfDay !in 0..23 || minute !in 0..59) {
                Log.e(TAG, "Invalid time range: $hourOfDay:$minute")
                return
            }
            
            scheduleCheckInReminder(checkInConfigId, checkInName, hourOfDay, minute)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse reminder time: $reminderTime", e)
        }
    }

    /**
     * Schedule daily reminder for a check-in item at specified time
     * @param checkInConfigId The ID of the check-in config
     * @param checkInName The name of the check-in item
     * @param hourOfDay Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    fun scheduleCheckInReminder(
        checkInConfigId: Long,
        checkInName: String,
        hourOfDay: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckInReminderReceiver::class.java).apply {
            putExtra(CheckInReminderReceiver.EXTRA_CHECK_IN_CONFIG_ID, checkInConfigId)
            putExtra(CheckInReminderReceiver.EXTRA_CHECK_IN_NAME, checkInName)
            putExtra(CheckInReminderReceiver.EXTRA_HOUR, hourOfDay)
            putExtra(CheckInReminderReceiver.EXTRA_MINUTE, minute)
        }
        
        // Use unique request code for each check-in item
        val requestCode = REMINDER_REQUEST_CODE_BASE + checkInConfigId.toInt()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
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
            // For Android 12+ (API 31+), use setExactAndAllowWhileIdle for precise timing
            // For older versions, use setRepeating
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                // Check if we can schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact check-in reminder scheduled for '$checkInName' at ${hourOfDay}:${minute.toString().padStart(2, '0')}")
                } else {
                    // Fall back to inexact alarm if exact alarms not allowed
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Inexact check-in reminder scheduled for '$checkInName' at ${hourOfDay}:${minute.toString().padStart(2, '0')}")
                }
            } else {
                // Use setRepeating for daily alarms on older Android versions
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
                Log.d(TAG, "Daily check-in reminder scheduled for '$checkInName' at ${hourOfDay}:${minute.toString().padStart(2, '0')}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule check-in reminder", e)
        }
    }

    /**
     * Cancel the scheduled reminder for a check-in item
     * @param checkInConfigId The ID of the check-in config
     */
    fun cancelCheckInReminder(checkInConfigId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckInReminderReceiver::class.java)
        
        val requestCode = REMINDER_REQUEST_CODE_BASE + checkInConfigId.toInt()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Check-in reminder canceled for config ID: $checkInConfigId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel check-in reminder", e)
        }
    }
}
