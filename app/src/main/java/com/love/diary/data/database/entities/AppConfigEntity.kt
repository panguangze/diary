package com.love.diary.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    
    val startDate: String,
    val startTimeMinutes: Int = 0,
    
    val coupleName: String? = null,
    val partnerNickname: String? = null,
    
    val showMoodTip: Boolean = true,
    val showStreak: Boolean = true,
    val showAnniversary: Boolean = true,
    
    val createdAt: Long,
    val updatedAt: Long,
    
    val reservedText1: String? = null,
    val reservedText2: String? = null,
    val reservedInt1: Int? = null,
    val reservedInt2: Int? = null
)
