package com.love.diary.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

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
    
    /**
     * Get appropriate text color (black or white) based on background luminance
     * Returns white for dark backgrounds, black for light backgrounds
     */
    fun getContrastingTextColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    }
}
