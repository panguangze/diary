package com.love.diary.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.FileProvider
import com.love.diary.data.model.MoodType
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Helper class for sharing content
 * Handles sharing of mood records as text or images
 */
class ShareHelper(private val context: Context) {

    companion object {
        private const val AUTHORITY = "com.love.diary.fileprovider"
        private const val SHARE_IMAGE_WIDTH = 1080
        private const val SHARE_IMAGE_HEIGHT = 1920
    }

    /**
     * Share mood record as text
     * @param date Date of the mood record
     * @param moodType The mood type
     * @param moodText Optional mood text
     * @param dayIndex Day count in relationship
     */
    fun shareMoodAsText(
        date: String,
        moodType: MoodType,
        moodText: String? = null,
        dayIndex: Int = 0
    ) {
        val shareText = buildString {
            append("📅 $date\n")
            append("💑 第 $dayIndex 天\n\n")
            append("${moodType.emoji} ${moodType.displayName}\n")
            if (!moodText.isNullOrBlank()) {
                append("\n$moodText\n")
            }
            append("\n来自异地恋日记 ❤️")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "今天的心情")
        }

        context.startActivity(Intent.createChooser(intent, "分享心情"))
    }

    /**
     * Share mood statistics as text
     * @param totalRecords Total number of records
     * @param daysTogether Days in relationship
     * @param topMood Most frequent mood
     * @param moodCount Count of top mood
     */
    fun shareStatisticsAsText(
        totalRecords: Int,
        daysTogether: Int,
        topMood: MoodType?,
        moodCount: Int = 0
    ) {
        val shareText = buildString {
            append("💕 我们的恋爱统计 💕\n\n")
            append("相识时间：$daysTogether 天\n")
            append("记录次数：$totalRecords 次\n\n")
            
            if (topMood != null) {
                append("最常心情：${topMood.emoji} ${topMood.displayName}\n")
                append("出现次数：$moodCount 次\n")
            }
            
            append("\n每一天都值得纪念 ❤️")
            append("\n来自异地恋日记")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "我们的统计")
        }

        context.startActivity(Intent.createChooser(intent, "分享统计"))
    }

    /**
     * Create and share mood card as image
     * @param date Date of the mood record
     * @param moodType The mood type
     * @param moodText Optional mood text
     * @param dayIndex Day count in relationship
     * @param coupleName Optional couple name
     */
    fun shareMoodAsImage(
        date: String,
        moodType: MoodType,
        moodText: String? = null,
        dayIndex: Int = 0,
        coupleName: String? = null
    ) {
        try {
            val bitmap = createMoodCardBitmap(
                date = date,
                moodType = moodType,
                moodText = moodText,
                dayIndex = dayIndex,
                coupleName = coupleName
            )

            val file = saveBitmapToCache(bitmap, "mood_card_${System.currentTimeMillis()}.png")
            val uri = FileProvider.getUriForFile(context, AUTHORITY, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "分享心情卡片"))
        } catch (e: Exception) {
            android.util.Log.e("ShareHelper", "Error sharing image", e)
            // Fallback to text sharing
            shareMoodAsText(date, moodType, moodText, dayIndex)
        }
    }

    /**
     * Create a mood card bitmap
     */
    private fun createMoodCardBitmap(
        date: String,
        moodType: MoodType,
        moodText: String?,
        dayIndex: Int,
        coupleName: String?
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(SHARE_IMAGE_WIDTH, SHARE_IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FFF5F5")
        }
        canvas.drawRect(0f, 0f, SHARE_IMAGE_WIDTH.toFloat(), SHARE_IMAGE_HEIGHT.toFloat(), bgPaint)
        
        // Title area
        val titlePaint = Paint().apply {
            color = Color.parseColor("#E91E63")
            textSize = 100f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText(coupleName ?: "我们的日记", SHARE_IMAGE_WIDTH / 2f, 200f, titlePaint)
        
        // Date
        val datePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(date, SHARE_IMAGE_WIDTH / 2f, 350f, datePaint)
        canvas.drawText("第 $dayIndex 天", SHARE_IMAGE_WIDTH / 2f, 450f, datePaint)
        
        // Mood emoji
        val emojiPaint = Paint().apply {
            textSize = 300f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(moodType.emoji, SHARE_IMAGE_WIDTH / 2f, 850f, emojiPaint)
        
        // Mood name
        val moodNamePaint = Paint().apply {
            color = Color.parseColor("#E91E63")
            textSize = 80f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText(moodType.displayName, SHARE_IMAGE_WIDTH / 2f, 1000f, moodNamePaint)
        
        // Mood text
        if (!moodText.isNullOrBlank()) {
            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 50f
                textAlign = Paint.Align.LEFT
                isAntiAlias = true
            }
            
            val maxWidth = SHARE_IMAGE_WIDTH - 200
            val lines = wrapText(moodText, textPaint, maxWidth.toFloat())
            var y = 1200f
            lines.forEach { line ->
                canvas.drawText(line, 100f, y, textPaint)
                y += 70f
            }
        }
        
        // Footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("来自异地恋日记 ❤️", SHARE_IMAGE_WIDTH / 2f, SHARE_IMAGE_HEIGHT - 100f, footerPaint)
        
        return bitmap
    }

    /**
     * Wrap text to fit within width
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)
            
            if (bounds.width() > maxWidth) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }

    /**
     * Save bitmap to cache directory
     */
    private fun saveBitmapToCache(bitmap: Bitmap, fileName: String): File {
        val cacheDir = File(context.cacheDir, "shared_images")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return file
    }
}
