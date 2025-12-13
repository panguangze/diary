package com.love.diary.di

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.EventDao
import com.love.diary.data.database.dao.UnifiedCheckInDao
import com.love.diary.data.repository.AppRepository
import com.love.diary.data.repository.UnifiedCheckInRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAppRepository(
        database: LoveDatabase,
        eventDao: EventDao
    ): AppRepository {
        return AppRepository(database, eventDao)
    }
    
    @Singleton
    @Provides
    fun provideUnifiedCheckInRepository(
        database: LoveDatabase
    ): UnifiedCheckInRepository {
        return UnifiedCheckInRepository(database)
    }
    
    @Singleton
    @Provides
    fun provideEventDao(database: LoveDatabase): EventDao {
        return database.eventDao()
    }
    
    @Singleton
    @Provides
    fun provideUnifiedCheckInDao(database: LoveDatabase): UnifiedCheckInDao {
        return database.unifiedCheckInDao()
    }
}