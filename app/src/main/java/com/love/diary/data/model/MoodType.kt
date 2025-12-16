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
    /** Happy/Joyful mood (highest positive score) */
    HAPPY(
        code = "HAPPY",
        score = 2,
        emoji = "😊",
        displayName = "开心",
        feedbackText = "开心收到啦，我也在屏幕这头偷偷笑～"
    ),
    
    /** Satisfied/Content mood */
    SATISFIED(
        code = "SATISFIED",
        score = 1,
        emoji = "💗",
        displayName = "满足",
        feedbackText = "能让你觉得满足，是我最大的幸运。"
    ),
    
    /** Normal/Neutral mood */
    NORMAL(
        code = "NORMAL",
        score = 0,
        emoji = "🙂",
        displayName = "正常",
        feedbackText = "平平淡淡的日子，就是细水长流的爱。"
    ),
    
    /** Sad/Down mood */
    SAD(
        code = "SAD",
        score = -1,
        emoji = "😔",
        displayName = "失落",
        feedbackText = "失落的时候，更想抱抱你。等我回到你身边，好吗？"
    ),
    
    /** Angry/Frustrated mood (lowest negative score) */
    ANGRY(
        code = "ANGRY",
        score = -2,
        emoji = "😡",
        displayName = "生气",
        feedbackText = "生气也没关系，你所有的情绪我都愿意听。"
    ),
    
    /** Custom/Other mood with user-provided text */
    OTHER(
        code = "OTHER",
        score = 0,
        emoji = "✏️",
        displayName = "其它",
        feedbackText = "我会好好读完你写的每一个字。"
    );

    /**
     * Get the corresponding drawable resource ID for the mood
     */
    fun getDrawableResourceId(): Int {
        return when (this) {
            HAPPY -> R.drawable.smile_yellow
            SATISFIED -> R.drawable.heart_pink
            NORMAL -> R.drawable.meh_gray
            SAD -> R.drawable.frown_blue
            ANGRY -> R.drawable.angry_red
            OTHER -> R.drawable.cry_blue  // 根据要求，"其他"替换为cry_blue.xml
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
         * @param tag The Chinese display name (e.g., "开心", "满足", etc.)
         * @return Matching MoodType or OTHER if not found
         */
        fun fromTag(tag: String?): MoodType {
            return when (tag) {
                "开心" -> HAPPY
                "满足" -> SATISFIED
                "正常" -> NORMAL
                "失落" -> SAD
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
                HAPPY -> "开心"
                SATISFIED -> "满足"
                NORMAL -> "正常"
                SAD -> "失落"
                ANGRY -> "生气"
                OTHER -> "其它"
            }
        }
    }
}
