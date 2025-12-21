package com.love.diary.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper object for camera operations
 */
object CameraHelper {
    
    /**
     * Create a temporary URI for camera capture
     * This URI will be used to save the captured photo temporarily
     */
    fun createImageUri(context: Context): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ use MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, generateImageFileName())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LoveDiary")
                }
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            } else {
                // For older versions, use FileProvider
                val imageFile = createImageFile(context)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Create a temporary image file for camera capture (used for Android < 10)
     */
    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "DIARY_$timeStamp"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }
    
    /**
     * Generate a unique filename for captured images
     */
    private fun generateImageFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "DIARY_$timeStamp.jpg"
    }
    
    /**
     * Save captured photo to gallery (for Android < 10)
     * For Android 10+, photos are automatically saved when using MediaStore
     */
    fun saveImageToGallery(context: Context, imageUri: Uri): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // For Android < 10, we need to manually add to gallery
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )?.let { newUri ->
                    context.contentResolver.openOutputStream(newUri)?.use { outputStream ->
                        context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
                true
            } else {
                // For Android 10+, image is already in gallery
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
