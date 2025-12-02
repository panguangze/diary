package com.love.diary.di

import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.repository.AppRepository
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
    fun provideAppRepository(database: LoveDatabase): AppRepository {
        return AppRepository(database)
    }
}