package com.love.diary.data.model

import com.love.diary.R

/**
 * Represents different mood types for daily entries
 * Each mood has an associated score, emoji, and feedback message
 * 
 * @property code Unique code identifier for the mood
 * @property score Numeric score for mood analysis (-2 to 2)
 * @property emoji Emoji representation of the mood
 * @property displayName User-facing display name
 * @property feedbackText Personalized feedback message shown to user
 */
enum class MoodType(
    val code: String,
    val score: Int,
    val emoji: String,
    val displayName: String,
    val feedbackText: String
) {
    /** Sweet/Romantic mood (highest positive score) */
    SWEET(
        code = "SWEET",
        score = 5,
        emoji = "🥰",
        displayName = "甜蜜",
        feedbackText = "甜蜜的日子，因为有你而更加珍贵。"
    ),
    
    /** Happy/Joyful mood */
    HAPPY(
        code = "HAPPY",
        score = 4,
        emoji = "😊",
        displayName = "开心",
        feedbackText = "看到你开心，我也感到无比幸福。"
    ),
    
    /** Normal/Neutral mood */
    NEUTRAL(
        code = "NEUTRAL",
        score = 3,
        emoji = "😐",
        displayName = "平淡",
        feedbackText = "平凡的日子里，有你的陪伴就是最大的温暖。"
    ),
    
    /** Sad/Down mood */
    SAD(
        code = "SAD",
        score = 2,
        emoji = "😔",
        displayName = "难过",
        feedbackText = "别难过，我会一直陪着你，一切都会好起来的。"
    ),
    
    /** Angry/Frustrated mood */
    ANGRY(
        code = "ANGRY",
        score = 1,
        emoji = "😡",
        displayName = "生气",
        feedbackText = "我知道你现在很生气，让我来哄哄你吧。"
    ),
    
    /** Custom/Other mood with user-provided text */
    OTHER(
        code = "OTHER",
        score = 3,
        emoji = "✏️",
        displayName = "其它",
        feedbackText = "无论怎样，我都爱你。"
    );

    /**
     * Get the corresponding drawable resource ID for the mood
     */
    fun getDrawableResourceId(): Int {
        return when (this) {
            SWEET -> R.drawable.heart_pink
            HAPPY -> R.drawable.smile_yellow
            NEUTRAL -> R.drawable.meh_gray
            SAD -> R.drawable.frown_blue
            ANGRY -> R.drawable.angry_red
            OTHER -> R.drawable.cry_blue
        }
    }
    
    
    companion object {
        /**
         * Get MoodType from code string
         * @param code The mood code to lookup
         * @return Matching MoodType or OTHER if not found
         */
        fun fromCode(code: String): MoodType {
            return values().find { it.code == code } ?: OTHER
        }
        
        /**
         * Get MoodType from Chinese display name tag
         * @param tag The Chinese display name (e.g., "甜蜜", "开心", etc.)
         * @return Matching MoodType or OTHER if not found
         */
        fun fromTag(tag: String?): MoodType {
            return when (tag) {
                "甜蜜" -> SWEET
                "开心" -> HAPPY
                "平淡" -> NEUTRAL
                "难过" -> SAD
                "生气" -> ANGRY
                else -> OTHER
            }
        }
        
        /**
         * Convert MoodType to Chinese display name tag
         * @param moodType The MoodType to convert
         * @return Chinese display name
         */
        fun toTag(moodType: MoodType): String {
            return when (moodType) {
                SWEET -> "甜蜜"
                HAPPY -> "开心"
                NEUTRAL -> "平淡"
                SAD -> "难过"
                ANGRY -> "生气"
                OTHER -> "其它"
            }
        }
    }
}
