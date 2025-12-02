package com.love.diary.di

import android.content.Context
import com.google.gson.Gson
import com.love.diary.data.backup.DataBackupManager
import com.love.diary.data.repository.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Singleton
    @Provides
    fun provideDataBackupManager(
        @ApplicationContext context: Context,
        repository: AppRepository,
        gson: Gson
    ): DataBackupManager {
        return DataBackupManager(context, repository, gson)
    }

    @Singleton
    @Provides
    fun provideGson(): Gson {
        return Gson()
    }
}