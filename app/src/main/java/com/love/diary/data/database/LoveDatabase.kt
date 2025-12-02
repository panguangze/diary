package com.love.diary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.love.diary.data.database.dao.AppConfigDao
import com.love.diary.data.database.dao.DailyMoodDao
import com.love.diary.data.database.entities.AppConfigEntity
import com.love.diary.data.database.entities.DailyMoodEntity

@Database(
    entities = [AppConfigEntity::class, DailyMoodEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LoveDatabase : RoomDatabase() {
    
    abstract fun appConfigDao(): AppConfigDao
    abstract fun dailyMoodDao(): DailyMoodDao
    
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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
