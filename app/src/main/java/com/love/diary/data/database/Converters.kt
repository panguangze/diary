package com.love.diary.data.database

import androidx.room.TypeConverter
import com.love.diary.data.model.PositiveDisplayType
import java.time.LocalDate

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { 
            LocalDate.ofEpochDay(it)
        }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromPositiveDisplayType(displayType: PositiveDisplayType): String {
        return displayType.name
    }

    @TypeConverter
    fun toPositiveDisplayType(value: String): PositiveDisplayType {
        return PositiveDisplayType.valueOf(value)
    }
}
