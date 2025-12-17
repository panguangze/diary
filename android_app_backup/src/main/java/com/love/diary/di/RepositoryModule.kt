package com.love.diary.di

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.dao.EventDao
import com.love.diary.data.database.dao.UnifiedCheckInDao
import com.love.diary.data.repository.AppRepository
import com.love.diary.data.repository.CheckInRepository
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
        eventDao: EventDao,
        checkInRepository: CheckInRepository
    ): AppRepository {
        return AppRepository(database, eventDao, checkInRepository)
    }
    
    @Singleton
    @Provides
    fun provideCheckInRepository(
        database: LoveDatabase
    ): CheckInRepository {
        return CheckInRepository(database)
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