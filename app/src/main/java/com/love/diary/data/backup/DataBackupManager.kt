// data/backup/DataBackupManager.kt
package com.love.diary.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.*
import com.love.diary.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manages data backup and restore operations
 * Handles export/import of app configuration, mood records, check-ins, and image files
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
        private const val BACKUP_DATA_FILE = "backup_data.json"
        private const val IMAGES_DIR = "images"
        private const val AVATARS_DIR = "avatars"
        private const val DATABASE_FILE = "database/love_diary.db"
    }

    /**
     * Data structure for backup files
     * @property backupDate Timestamp when backup was created
     * @property appConfig Application configuration (includes avatar URIs in reservedText1/reservedText2)
     * @property moodRecords List of all mood entries
     * @property checkInRecords List of all check-in records
     * @property checkInConfigs List of all check-in configurations (includes reminder settings)
     * @property habits List of all habits (legacy system)
     * @property habitRecords List of all habit records (legacy system)
     * @property events List of all events
     * @property eventConfigs List of all event configurations
     * @property imageFiles List of image file paths (relative to backup)
     * @property avatarFiles List of avatar file paths (user and partner avatars)
     */
    data class BackupData(
        val backupDate: String,
        val appConfig: com.love.diary.data.database.entities.AppConfigEntity?,
        val moodRecords: List<DailyMoodEntity>,
        val checkInRecords: List<UnifiedCheckIn> = emptyList(),
        val checkInConfigs: List<UnifiedCheckInConfig> = emptyList(),
        val habits: List<Habit> = emptyList(),
        val habitRecords: List<HabitRecord> = emptyList(),
        val events: List<Event> = emptyList(),
        val eventConfigs: List<EventConfig> = emptyList(),
        val imageFiles: List<String> = emptyList(),
        val avatarFiles: List<String> = emptyList()
    )

    /**
     * Export data to internal storage (legacy method - JSON only)
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
     * Export data with images to user-selected URI as ZIP file
     * Exports all data including:
     * - App configuration (with reminder settings)
     * - Mood records
     * - Check-in records and configurations
     * - Habits and habit records (legacy system)
     * - Events and event configurations
     * - All images (mood attachments, check-in attachments)
     * - Avatar images (user and partner)
     * - Raw database file (as additional backup)
     * @param uri Target URI for export
     * @return Result indicating success or failure
     */
    suspend fun exportDataToUri(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting comprehensive export to URI: $uri")
            
            // 获取所有数据
            val config = repository.getAppConfig()
            val moodRecords = repository.getAllMoodRecords()
            val checkInRecords = repository.getAllCheckInRecords()
            val checkInConfigs = repository.getAllCheckInConfigsForBackup()
            val habits = repository.getAllHabitsForBackup()
            val habitRecords = repository.getAllHabitRecords()
            val events = repository.getAllEvents()
            val eventConfigs = repository.getAllEventConfigs()
            
            if (config == null) {
                Log.w(TAG, "No config found, exporting records only")
            }

            // 收集所有内容图片URI
            val imageUris = mutableListOf<String>()
            moodRecords.forEach { mood ->
                mood.singleImageUri?.let { imageUris.add(it) }
            }
            checkInRecords.forEach { checkIn ->
                checkIn.attachmentUri?.let { imageUris.add(it) }
            }
            
            // 收集头像URI
            val avatarUris = mutableListOf<String>()
            config?.reservedText1?.let { avatarUris.add(it) } // User avatar
            config?.reservedText2?.let { avatarUris.add(it) } // Partner avatar
            
            Log.d(TAG, "Found ${imageUris.size} content images and ${avatarUris.size} avatars to backup")
            Log.d(TAG, "Data counts - Moods: ${moodRecords.size}, CheckIns: ${checkInRecords.size}, " +
                    "CheckInConfigs: ${checkInConfigs.size}, Habits: ${habits.size}, " +
                    "HabitRecords: ${habitRecords.size}, Events: ${events.size}, EventConfigs: ${eventConfigs.size}")

            val backupData = BackupData(
                backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                appConfig = config,
                moodRecords = moodRecords,
                checkInRecords = checkInRecords,
                checkInConfigs = checkInConfigs,
                habits = habits,
                habitRecords = habitRecords,
                events = events,
                eventConfigs = eventConfigs,
                imageFiles = imageUris.distinct(),
                avatarFiles = avatarUris.distinct()
            )

            // 创建ZIP文件
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // 1. 写入JSON数据
                    val jsonData = gson.toJson(backupData)
                    val jsonEntry = ZipEntry(BACKUP_DATA_FILE)
                    zipOut.putNextEntry(jsonEntry)
                    zipOut.write(jsonData.toByteArray())
                    zipOut.closeEntry()
                    
                    // 2. 写入内容图片文件
                    imageUris.distinct().forEachIndexed { index, imageUriStr ->
                        try {
                            val imageUri = Uri.parse(imageUriStr)
                            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                                // 使用索引作为文件名，保持原扩展名
                                val extension = getFileExtension(imageUriStr)
                                val imageFileName = "$IMAGES_DIR/image_$index$extension"
                                
                                val imageEntry = ZipEntry(imageFileName)
                                zipOut.putNextEntry(imageEntry)
                                
                                inputStream.copyTo(zipOut)
                                zipOut.closeEntry()
                                
                                Log.d(TAG, "Backed up content image: $imageFileName")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to backup content image: $imageUriStr", e)
                            // Continue with other images
                        }
                    }
                    
                    // 3. 写入头像文件
                    avatarUris.distinct().forEachIndexed { index, avatarUriStr ->
                        try {
                            val avatarUri = Uri.parse(avatarUriStr)
                            context.contentResolver.openInputStream(avatarUri)?.use { inputStream ->
                                // 使用索引作为文件名，保持原扩展名
                                val extension = getFileExtension(avatarUriStr)
                                val avatarFileName = "$AVATARS_DIR/avatar_$index$extension"
                                
                                val avatarEntry = ZipEntry(avatarFileName)
                                zipOut.putNextEntry(avatarEntry)
                                
                                inputStream.copyTo(zipOut)
                                zipOut.closeEntry()
                                
                                Log.d(TAG, "Backed up avatar: $avatarFileName")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to backup avatar: $avatarUriStr", e)
                            // Continue with other avatars
                        }
                    }
                    
                    // 4. 写入数据库文件（作为额外备份）
                    try {
                        val dbFile = context.getDatabasePath("love_diary.db")
                        if (dbFile.exists()) {
                            FileInputStream(dbFile).use { inputStream ->
                                val dbEntry = ZipEntry(DATABASE_FILE)
                                zipOut.putNextEntry(dbEntry)
                                inputStream.copyTo(zipOut)
                                zipOut.closeEntry()
                                Log.d(TAG, "Backed up database file")
                            }
                        } else {
                            Log.w(TAG, "Database file not found at: ${dbFile.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to backup database file", e)
                        // Continue - database backup is optional since we have JSON
                    }
                }
            } ?: throw IOException("无法打开输出流")

            Log.d(TAG, "Comprehensive export successful - All data and media backed up")
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
     * Import data with images from user-selected URI (ZIP file)
     * Restores all data including:
     * - App configuration (with reminder settings)
     * - Mood records
     * - Check-in records and configurations
     * - Habits and habit records (legacy system)
     * - Events and event configurations
     * - All images (mood attachments, check-in attachments)
     * - Avatar images (user and partner)
     * @param uri Source URI for import
     * @return Result indicating success or failure
     */
    suspend fun importData(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting comprehensive import from URI: $uri")
            
            // 创建临时目录存储解压的文件
            val tempDir = File(context.cacheDir, "import_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            var backupData: BackupData? = null
            val imageMapping = mutableMapOf<String, String>() // old path -> new path
            val avatarMapping = mutableMapOf<String, String>() // old path -> new path
            
            try {
                // 解压ZIP文件
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry: ZipEntry?
                        while (zipIn.nextEntry.also { entry = it } != null) {
                            val entryName = entry!!.name

                            if (entryName == BACKUP_DATA_FILE) {
                                // 读取JSON数据
                                val jsonData = String(zipIn.readBytes(), Charsets.UTF_8)

                                if (jsonData.isBlank()) {
                                    throw IllegalArgumentException("备份文件为空")
                                }

                                backupData = try {
                                    gson.fromJson(jsonData, BackupData::class.java)
                                } catch (e: Exception) {
                                    throw IllegalArgumentException("备份文件格式无效", e)
                                }

                                Log.d(TAG, "Parsed backup data from ${backupData.backupDate}")
                            } else if (entryName.startsWith(IMAGES_DIR)) {
                                // 提取内容图片文件
                                val imageFile = File(tempDir, entryName)
                                imageFile.parentFile?.mkdirs()
                                
                                FileOutputStream(imageFile).use { output ->
                                    zipIn.copyTo(output)
                                }
                                
                                Log.d(TAG, "Extracted content image: ${imageFile.name}")
                            } else if (entryName.startsWith(AVATARS_DIR)) {
                                // 提取头像文件
                                val avatarFile = File(tempDir, entryName)
                                avatarFile.parentFile?.mkdirs()
                                
                                FileOutputStream(avatarFile).use { output ->
                                    zipIn.copyTo(output)
                                }
                                
                                Log.d(TAG, "Extracted avatar: ${avatarFile.name}")
                            }
                        }
                    }
                }
                
                if (backupData == null) {
                    throw IllegalArgumentException("备份文件中没有找到数据")
                }
                
                // 将临时内容图片文件复制到应用的图片目录
                val appImagesDir = File(context.filesDir, "images")
                appImagesDir.mkdirs()
                
                val extractedImagesDir = File(tempDir, IMAGES_DIR)
                if (extractedImagesDir.exists()) {
                    extractedImagesDir.listFiles()?.forEach { imageFile ->
                        val newImageFile = File(appImagesDir, "${System.currentTimeMillis()}_${imageFile.name}")
                        imageFile.copyTo(newImageFile, overwrite = true)
                        
                        // 记录旧路径到新路径的映射
                        val oldIndex = imageFile.nameWithoutExtension.substringAfter("image_").toIntOrNull()
                        if (oldIndex != null && oldIndex < backupData!!.imageFiles.size) {
                            val oldPath = backupData!!.imageFiles[oldIndex]
                            val newUri = Uri.fromFile(newImageFile).toString()
                            imageMapping[oldPath] = newUri
                            Log.d(TAG, "Mapped content image: $oldPath -> $newUri")
                        }
                    }
                }
                
                // 将临时头像文件复制到应用的图片目录
                val extractedAvatarsDir = File(tempDir, AVATARS_DIR)
                if (extractedAvatarsDir.exists()) {
                    extractedAvatarsDir.listFiles()?.forEach { avatarFile ->
                        val newAvatarFile = File(appImagesDir, "${System.currentTimeMillis()}_${avatarFile.name}")
                        avatarFile.copyTo(newAvatarFile, overwrite = true)
                        
                        // 记录旧路径到新路径的映射
                        val oldIndex = avatarFile.nameWithoutExtension.substringAfter("avatar_").toIntOrNull()
                        if (oldIndex != null && oldIndex < backupData!!.avatarFiles.size) {
                            val oldPath = backupData!!.avatarFiles[oldIndex]
                            val newUri = Uri.fromFile(newAvatarFile).toString()
                            avatarMapping[oldPath] = newUri
                            Log.d(TAG, "Mapped avatar: $oldPath -> $newUri")
                        }
                    }
                }
                
                // 恢复应用配置（包含头像路径）
                backupData!!.appConfig?.let { config ->
                    // 更新头像URI
                    val updatedConfig = config.copy(
                        reservedText1 = config.reservedText1?.let { avatarMapping[it] } ?: config.reservedText1,
                        reservedText2 = config.reservedText2?.let { avatarMapping[it] } ?: config.reservedText2
                    )
                    repository.saveAppConfig(updatedConfig)
                    Log.d(TAG, "Config restored with updated avatar paths")
                }

                // 清空现有记录并导入新记录（更新图片路径）
                repository.clearAllMoodRecords()
                if (backupData!!.moodRecords.isNotEmpty()) {
                    val updatedMoodRecords = backupData!!.moodRecords.map { mood ->
                        mood.singleImageUri?.let { oldUri ->
                            val newUri = imageMapping[oldUri]
                            if (newUri != null) {
                                mood.copy(singleImageUri = newUri)
                            } else {
                                mood
                            }
                        } ?: mood
                    }
                    repository.batchInsertMoodRecords(updatedMoodRecords)
                    Log.d(TAG, "Restored ${updatedMoodRecords.size} mood records")
                }
                
                // 恢复check-in记录
                if (backupData!!.checkInRecords.isNotEmpty()) {
                    val updatedCheckInRecords = backupData!!.checkInRecords.map { checkIn ->
                        checkIn.attachmentUri?.let { oldUri ->
                            val newUri = imageMapping[oldUri]
                            if (newUri != null) {
                                checkIn.copy(attachmentUri = newUri)
                            } else {
                                checkIn
                            }
                        } ?: checkIn
                    }
                    repository.batchInsertCheckInRecords(updatedCheckInRecords)
                    Log.d(TAG, "Restored ${updatedCheckInRecords.size} check-in records")
                }
                
                // 恢复check-in配置
                if (backupData!!.checkInConfigs.isNotEmpty()) {
                    repository.batchInsertCheckInConfigs(backupData!!.checkInConfigs)
                    Log.d(TAG, "Restored ${backupData!!.checkInConfigs.size} check-in configs")
                }
                
                // 恢复习惯（legacy系统）
                if (backupData!!.habits.isNotEmpty()) {
                    repository.batchInsertHabits(backupData!!.habits)
                    Log.d(TAG, "Restored ${backupData!!.habits.size} habits")
                }
                
                // 恢复习惯记录
                if (backupData!!.habitRecords.isNotEmpty()) {
                    repository.batchInsertHabitRecords(backupData!!.habitRecords)
                    Log.d(TAG, "Restored ${backupData!!.habitRecords.size} habit records")
                }
                
                // 恢复事件
                if (backupData!!.events.isNotEmpty()) {
                    repository.batchInsertEvents(backupData!!.events)
                    Log.d(TAG, "Restored ${backupData!!.events.size} events")
                }
                
                // 恢复事件配置
                if (backupData!!.eventConfigs.isNotEmpty()) {
                    repository.batchInsertEventConfigs(backupData!!.eventConfigs)
                    Log.d(TAG, "Restored ${backupData!!.eventConfigs.size} event configs")
                }

                Log.d(TAG, "Comprehensive import successful - All data and media restored")
                Result.success(true)
            } finally {
                // 清理临时文件
                tempDir.deleteRecursively()
            }
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
    
    /**
     * Get file extension from URI string
     */
    private fun getFileExtension(uriStr: String): String {
        return when {
            uriStr.contains(".jpg", ignoreCase = true) || uriStr.contains(".jpeg", ignoreCase = true) -> ".jpg"
            uriStr.contains(".png", ignoreCase = true) -> ".png"
            uriStr.contains(".gif", ignoreCase = true) -> ".gif"
            uriStr.contains(".webp", ignoreCase = true) -> ".webp"
            else -> ".jpg" // default
        }
    }
}