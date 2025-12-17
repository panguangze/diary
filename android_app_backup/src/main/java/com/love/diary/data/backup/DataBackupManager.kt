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

/**
 * Manages data backup and restore operations
 * Handles export/import of app configuration and mood records
 * 
 * @property context Android application context
 * @property repository Repository for data access
 * @property gson JSON serializer/deserializer
 */
class DataBackupManager(
    private val context: Context,
    private val repository: AppRepository,
    private val gson: Gson = Gson()
) {

    companion object {
        private const val TAG = "DataBackupManager"
    }

    /**
     * Data structure for backup files
     * @property backupDate Timestamp when backup was created
     * @property appConfig Application configuration
     * @property moodRecords List of all mood entries
     */
    data class BackupData(
        val backupDate: String,
        val appConfig: com.love.diary.data.database.entities.AppConfigEntity?,
        val moodRecords: List<DailyMoodEntity>
    )

    /**
     * Export data to internal storage
     * @return Result containing file path on success, or error
     */
    suspend fun exportData(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data export")
            
            // 获取数据
            val config = repository.getAppConfig()
            val records = repository.getAllMoodRecords()
            
            if (records.isEmpty()) {
                Log.w(TAG, "No mood records to export")
            }

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
            
            Log.d(TAG, "Export successful: ${file.absolutePath}")
            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during export", e)
            Result.failure(IOException("无法写入文件: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during export", e)
            Result.failure(Exception("导出失败: ${e.message}", e))
        }
    }

    /**
     * Export data to user-selected URI
     * @param uri Target URI for export
     * @return Result indicating success or failure
     */
    suspend fun exportDataToUri(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting export to URI: $uri")
            
            // 获取数据
            val config = repository.getAppConfig()
            val records = repository.getAllMoodRecords()
            
            if (config == null) {
                Log.w(TAG, "No config found, exporting records only")
            }

            val backupData = BackupData(
                backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                appConfig = config,
                moodRecords = records
            )

            // 转换为JSON
            val jsonData = gson.toJson(backupData)

            // 写入到URI指定的文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonData.toByteArray())
                outputStream.flush()
            } ?: throw IOException("无法打开输出流")

            Log.d(TAG, "Export to URI successful")
            Result.success(true)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during export to URI", e)
            Result.failure(IOException("无法写入文件: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during export to URI", e)
            Result.failure(Exception("导出失败: ${e.message}", e))
        }
    }

    /**
     * Import data from user-selected URI
     * @param uri Source URI for import
     * @return Result indicating success or failure
     */
    suspend fun importData(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting import from URI: $uri")
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonData = inputStream.bufferedReader().use { it.readText() }
                
                if (jsonData.isBlank()) {
                    throw IllegalArgumentException("备份文件为空")
                }
                
                val backupData = try {
                    gson.fromJson(jsonData, BackupData::class.java)
                } catch (e: Exception) {
                    throw IllegalArgumentException("备份文件格式无效", e)
                }

                Log.d(TAG, "Parsed backup data: ${backupData.moodRecords.size} records")

                // 恢复数据
                backupData.appConfig?.let { config ->
                    repository.saveAppConfig(config)
                    Log.d(TAG, "Config restored")
                }

                // 清空现有记录并导入新记录
                repository.clearAllMoodRecords()
                if (backupData.moodRecords.isNotEmpty()) {
                    repository.batchInsertMoodRecords(backupData.moodRecords)
                    Log.d(TAG, "Restored ${backupData.moodRecords.size} mood records")
                }

                Result.success(true)
            } ?: Result.failure(IOException("无法读取文件"))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid backup file", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during import", e)
            Result.failure(IOException("无法读取文件: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during import", e)
            Result.failure(Exception("导入失败: ${e.message}", e))
        }
    }
}