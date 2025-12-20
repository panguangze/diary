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
import com.love.diary.data.database.dao.UnifiedCheckInDao
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.database.entities.DailyMoodEntity
import com.love.diary.data.model.Event
import com.love.diary.data.model.EventConfig
import com.love.diary.data.model.Habit
import com.love.diary.data.model.HabitRecord
import com.love.diary.data.model.UnifiedCheckIn
import com.love.diary.data.model.UnifiedCheckInConfig

@Database(
    entities = [AppConfigEntity::class, DailyMoodEntity::class, Habit::class, HabitRecord::class, Event::class, EventConfig::class, UnifiedCheckIn::class, UnifiedCheckInConfig::class],
    version = 12,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LoveDatabase : RoomDatabase() {
    
    abstract fun appConfigDao(): AppConfigDao
    abstract fun dailyMoodDao(): DailyMoodDao
    abstract fun habitDao(): HabitDao
    abstract fun eventDao(): EventDao
    abstract fun unifiedCheckInDao(): UnifiedCheckInDao
    
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
                .addMigrations(
                    MigrationHelper.MIGRATION_4_5, 
                    MigrationHelper.MIGRATION_5_6, 
                    MigrationHelper.MIGRATION_6_7,
                    MigrationHelper.MIGRATION_7_8,
                    MigrationHelper.MIGRATION_8_9,
                    MigrationHelper.MIGRATION_9_10,
                    MigrationHelper.MIGRATION_10_11,
                    MigrationHelper.MIGRATION_11_12
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
