package com.love.diary.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.love.diary.data.database.LoveDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit

/**
 * 后台任务：定期重新安排提醒，避免系统回收后提醒失效
 */
class ReminderRescheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val database: LoveDatabase by lazy { LoveDatabase.getInstance(applicationContext) }

    companion object {
        private const val PERIODIC_WORK_NAME = "reminder_reschedule_periodic"
        private const val ONE_TIME_WORK_NAME = "reminder_reschedule_once"

        /**
         * 启动一次后台任务，用于立即和周期性地同步提醒
         */
        fun enqueue(context: Context) {
            val workManager = WorkManager.getInstance(context)

            // 立即执行一次，确保当前提醒已设置
            val oneTimeRequest = OneTimeWorkRequestBuilder<ReminderRescheduleWorker>().build()
            workManager.enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )

            // 周期性执行，定期校准提醒（每12小时）
            val periodicRequest = PeriodicWorkRequestBuilder<ReminderRescheduleWorker>(
                12,
                TimeUnit.HOURS
            ).build()
            workManager.enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        val reminderScheduler = ReminderScheduler(context)

        // 重新安排每日心情提醒
        val appConfig = database.appConfigDao().getConfig()
        if (appConfig?.reminderEnabled == true) {
            reminderScheduler.scheduleDailyReminder(appConfig.reminderTime)
        }

        // 重新安排打卡提醒（设置超时避免阻塞）
        val checkInConfigs = withTimeoutOrNull(3_000) {
            database.unifiedCheckInDao().getAllCheckInConfigs().first()
        }.orEmpty()

        val checkInScheduler = CheckInReminderScheduler(context)
        checkInConfigs.forEach { config ->
            val reminderTime = config.reminderTime
            if (config.isActive && !reminderTime.isNullOrBlank()) {
                checkInScheduler.scheduleCheckInReminder(
                    config.id,
                    config.name,
                    reminderTime
                )
            }
        }

        Result.success()
    }
}
