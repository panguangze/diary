// data/backup/DataBackupManager.kt
package com.love.diary.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class DataBackupManager(
    private val context: Context,
    private val repository: AppRepository,
    private val gson: Gson = Gson()
) {

    data class BackupData(
        val backupDate: String,
        val appConfig: com.love.diary.data.database.entities.AppConfigEntity?,
        val moodRecords: List<DailyMoodEntity>
    )

    suspend fun exportData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 获取数据
            val config = repository.getAppConfig()
            val records = repository.getAllMoodRecords()

            val backupData = BackupData(
                backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                appConfig = config,
                moodRecords = records
            )

            // 转换为JSON
            val jsonData = gson.toJson(backupData)

            // 保存到文件
            val fileName = "love_diary_backup_${System.currentTimeMillis()}.json"
            val file = File(context.getExternalFilesDir(null), fileName)

            file.bufferedWriter().use { writer ->
                writer.write(jsonData)
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonData = inputStream.bufferedReader().use { it.readText() }
                val backupData = gson.fromJson(jsonData, BackupData::class.java)

                // 恢复数据
                backupData.appConfig?.let { config ->
                    repository.saveAppConfig(config)
                }

                // 清空现有记录并导入新记录
                // TODO: 需要添加清空和批量插入方法到Repository

                Result.success(true)
            } ?: Result.failure(IOException("无法读取文件"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}