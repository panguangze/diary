package com.love.diary.util

import androidx.compose.ui.graphics.Color

/**
 * Utility functions for color parsing and conversion
 */
object ColorUtil {
    /**
     * Parse a color string (e.g., "#FF5722") to a Compose Color
     * Returns null if parsing fails
     */
    fun parseColor(colorString: String?): Color? {
        if (colorString == null) return null
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            null
        }
    }
}
