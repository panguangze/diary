package com.love.diary.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.love.diary.data.database.entities.AppConfigEntity

@Dao
interface AppConfigDao {
    
    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getConfig(): AppConfigEntity?
    
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getConfigFlow(): kotlinx.coroutines.flow.Flow<AppConfigEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfigEntity)
    
    @Update
    suspend fun updateConfig(config: AppConfigEntity)
    
    @Query("DELETE FROM app_config WHERE id = 1")
    suspend fun deleteConfig()
}
