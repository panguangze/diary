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
    
    // 从版本5到版本6的迁移 - 更新checkins和checkin_configs表结构
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 添加新列到checkins表
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `note` TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `attachmentUri` TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `duration` INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `rating` INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `isCompleted` INTEGER DEFAULT 1")
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `metadata` TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE `checkins` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
            
            // 添加新列到checkin_configs表
            database.execSQL("ALTER TABLE `checkin_configs` ADD COLUMN `targetValue` INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE `checkin_configs` ADD COLUMN `reminderTime` TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE `checkin_configs` ADD COLUMN `isRecurring` INTEGER DEFAULT 0")
            database.execSQL("ALTER TABLE `checkin_configs` ADD COLUMN `recurrencePattern` TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE `checkin_configs` ADD COLUMN `metadata` TEXT DEFAULT ''")
            database.execSQL("ALTER TABLE `checkin_configs` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
        }
    }
}