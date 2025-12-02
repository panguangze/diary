package com.love.diary.di

import android.content.Context
import androidx.room.Room
import com.love.diary.data.database.LoveDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideLoveDatabase(@ApplicationContext context: Context): LoveDatabase {
        return Room.databaseBuilder(
            context,
            LoveDatabase::class.java,
            "love_diary_database"
        )
        .fallbackToDestructiveMigration() // 在开发阶段使用，生产环境可能需要更安全的迁移策略
        .build()
    }
}