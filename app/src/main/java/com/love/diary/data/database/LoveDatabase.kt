package com.love.diary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.love.diary.data.database.dao.AppConfigDao
import com.love.diary.data.database.dao.DailyMoodDao
import com.love.diary.data.database.dao.EventDao
import com.love.diary.data.database.dao.HabitDao
import com.love.diary.data.database.dao.CheckInDao
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.Event
import com.love.diary.data.model.EventConfig
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import com.love.diary.data.model.CheckIn
import com.love.diary.data.model.CheckInConfig

@Database(
    entities = [AppConfigEntity::class, DailyMoodEntity::class, Habit::class, HabitRecord::class, Event::class, EventConfig::class, CheckIn::class, CheckInConfig::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LoveDatabase : RoomDatabase() {
    
    abstract fun appConfigDao(): AppConfigDao
    abstract fun dailyMoodDao(): DailyMoodDao
    abstract fun habitDao(): HabitDao
    abstract fun eventDao(): EventDao
    abstract fun checkInDao(): CheckInDao
    
    companion object {
        @Volatile
        private var INSTANCE: LoveDatabase? = null
        
        fun getInstance(context: Context): LoveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoveDatabase::class.java,
                    "love_diary.db"
                )
                .addMigrations(MigrationHelper.MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
