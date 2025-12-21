// data/backup/DataBackupManager.kt
package com.love.diary.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.UnifiedCheckIn
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
    }

    /**
     * Data structure for backup files
     * @property backupDate Timestamp when backup was created
     * @property appConfig Application configuration
     * @property moodRecords List of all mood entries
     * @property checkInRecords List of all check-in records
     * @property imageFiles List of image file paths (relative to backup)
     */
    data class BackupData(
        val backupDate: String,
        val appConfig: com.love.diary.data.database.entities.AppConfigEntity?,
        val moodRecords: List<DailyMoodEntity>,
        val checkInRecords: List<UnifiedCheckIn> = emptyList(),
        val imageFiles: List<String> = emptyList()
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
     * @param uri Target URI for export
     * @return Result indicating success or failure
     */
    suspend fun exportDataToUri(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting export with images to URI: $uri")
            
            // 获取数据
            val config = repository.getAppConfig()
            val moodRecords = repository.getAllMoodRecords()
            val checkInRecords = repository.getAllCheckInRecords()
            
            if (config == null) {
                Log.w(TAG, "No config found, exporting records only")
            }

            // 收集所有图片URI
            val imageUris = mutableListOf<String>()
            moodRecords.forEach { mood ->
                mood.singleImageUri?.let { imageUris.add(it) }
            }
            checkInRecords.forEach { checkIn ->
                checkIn.attachmentUri?.let { imageUris.add(it) }
            }
            
            Log.d(TAG, "Found ${imageUris.size} images to backup")

            val backupData = BackupData(
                backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                appConfig = config,
                moodRecords = moodRecords,
                checkInRecords = checkInRecords,
                imageFiles = imageUris.distinct()
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
                    
                    // 2. 写入图片文件
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
                                
                                Log.d(TAG, "Backed up image: $imageFileName")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to backup image: $imageUriStr", e)
                            // Continue with other images
                        }
                    }
                }
            } ?: throw IOException("无法打开输出流")

            Log.d(TAG, "Export with images to URI successful")
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
     * @param uri Source URI for import
     * @return Result indicating success or failure
     */
    suspend fun importData(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting import with images from URI: $uri")
            
            // 创建临时目录存储解压的图片
            val tempDir = File(context.cacheDir, "import_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            var backupData: BackupData? = null
            val imageMapping = mutableMapOf<String, String>() // old path -> new path
            
            try {
                // 解压ZIP文件
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                        var entry: ZipEntry?
                        while (zipIn.nextEntry.also { entry = it } != null) {
                            val entryName = entry!!.name

                            if (entryName == BACKUP_DATA_FILE) {
                                // 修正：直接读取字节并转换为 String，不关闭流
                                // ZipInputStream 的 readBytes() 会读取当前 Entry 直到结束，不会关闭流
                                val jsonData = String(zipIn.readBytes(), Charsets.UTF_8)

                                // 或者使用这种方式（不加 .use）:
                                // val jsonData = zipIn.bufferedReader().readText()

                                if (jsonData.isBlank()) {
                                    throw IllegalArgumentException("备份文件为空")
                                }

                                backupData = try {
                                    gson.fromJson(jsonData, BackupData::class.java)
                                } catch (e: Exception) {
                                    throw IllegalArgumentException("备份文件格式无效", e)
                                }

                                Log.d(TAG, "Parsed backup data: ...")
                            }else if (entryName.startsWith(IMAGES_DIR)) {
                                // 提取图片文件
                                val imageFile = File(tempDir, entryName)
                                imageFile.parentFile?.mkdirs()
                                
                                FileOutputStream(imageFile).use { output ->
                                    zipIn.copyTo(output)
                                }
                                
                                Log.d(TAG, "Extracted image: ${imageFile.name}")
                            }
                        }
                    }
                }
                
                if (backupData == null) {
                    throw IllegalArgumentException("备份文件中没有找到数据")
                }
                
                // 将临时图片文件复制到应用的图片目录
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
                            Log.d(TAG, "Mapped image: $oldPath -> $newUri")
                        }
                    }
                }
                
                // 恢复数据
                backupData!!.appConfig?.let { config ->
                    repository.saveAppConfig(config)
                    Log.d(TAG, "Config restored")
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