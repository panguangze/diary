package com.love.diary.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移辅助类
 * 处理从旧版本到新版本的数据库迁移
 */
object MigrationHelper {
    
    // 从版本4到版本5的迁移 - 添加新的events和event_configs表
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建新的events表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `events` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `moodType` TEXT,
                    `habitId` INTEGER,
                    `tag` TEXT,
                    `date` TEXT NOT NULL,
                    `count` INTEGER NOT NULL,
                    `note` TEXT,
                    `metadata` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """)
            
            // 创建新的event_configs表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `event_configs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `description` TEXT,
                    `buttonLabel` TEXT NOT NULL,
                    `targetDate` TEXT,
                    `startDate` TEXT NOT NULL,
                    `icon` TEXT NOT NULL,
                    `color` TEXT NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    `metadata` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """)
        }
    }
}