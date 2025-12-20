package com.love.diary.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Application configuration entity stored in Room database
 * Stores user preferences and app settings
 */
@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    
    /** Start date of the relationship in yyyy-MM-dd format */
    val startDate: String,
    val startTimeMinutes: Int = 0,
    
    /** Display name for the couple */
    val coupleName: String? = null,
    /** Nickname for partner */
    val partnerNickname: String? = null,
    
    /** Show mood feedback tips on home screen */
    val showMoodTip: Boolean = true,
    /** Show streak counter */
    val showStreak: Boolean = true,
    /** Show anniversary notifications */
    val showAnniversary: Boolean = true,
    /** Dark mode setting: null = system, true = dark, false = light */
    val darkMode: Boolean? = null,
    
    /** Daily reminder enabled */
    val reminderEnabled: Boolean = false,
    /** Daily reminder time in minutes from midnight (0-1439) */
    val reminderTime: Int = 540, // Default: 9:00 AM (9 * 60)
    
    val createdAt: Long,
    val updatedAt: Long,
    
    val reservedText1: String? = null,
    val reservedText2: String? = null,
    val reservedInt1: Int? = null,
    val reservedInt2: Int? = null
)
