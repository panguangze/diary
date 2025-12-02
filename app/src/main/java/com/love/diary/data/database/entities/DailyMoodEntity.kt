package com.love.diary.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_mood",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["dayIndex"]),
        Index(value = ["attachmentGroupId"])
    ]
)
data class DailyMoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    val date: String,
    val dayIndex: Int,
    
    val moodTypeCode: String,
    val moodScore: Int = 0,
    
    val moodText: String? = null,
    val hasText: Boolean = false,
    
    val singleImageUri: String? = null,
    val singleImageType: String? = null,
    val attachmentGroupId: String? = null,
    
    val isAnniversary: Boolean = false,
    val anniversaryType: String? = null,
    
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean = false,
    
    val reservedText1: String? = null,
    val reservedText2: String? = null,
    val reservedInt1: Int? = null,
    val reservedInt2: Int? = null
)
