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
    
    // Helper function to safely add a column if it doesn't already exist
    private fun addColumnIfNotExists(
        database: SupportSQLiteDatabase,
        tableName: String,
        columnName: String,
        columnDefinition: String
    ) {
        // Check if column already exists
        val cursor = database.query("PRAGMA table_info($tableName)")
        var columnExists = false
        while (cursor.moveToNext()) {
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                columnExists = true
                break
            }
        }
        cursor.close()
        
        // Only add the column if it doesn't exist
        if (!columnExists) {
            database.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
        }
    }

    // 从版本5到版本6的迁移 - 更新checkins和checkin_configs表结构
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 添加新列到checkins表
            addColumnIfNotExists(database, "checkins", "note", "TEXT DEFAULT ''")
            addColumnIfNotExists(database, "checkins", "attachmentUri", "TEXT DEFAULT ''")
            addColumnIfNotExists(database, "checkins", "duration", "INTEGER DEFAULT 0")
            addColumnIfNotExists(database, "checkins", "rating", "INTEGER DEFAULT 0")
            addColumnIfNotExists(database, "checkins", "isCompleted", "INTEGER DEFAULT 1")
            addColumnIfNotExists(database, "checkins", "metadata", "TEXT DEFAULT ''")
            addColumnIfNotExists(database, "checkins", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
            
            // 添加新列到checkin_configs表
            addColumnIfNotExists(database, "checkin_configs", "targetValue", "INTEGER DEFAULT 0")
            addColumnIfNotExists(database, "checkin_configs", "reminderTime", "TEXT DEFAULT ''")
            addColumnIfNotExists(database, "checkin_configs", "isRecurring", "INTEGER DEFAULT 0")
            addColumnIfNotExists(database, "checkin_configs", "recurrencePattern", "TEXT DEFAULT ''")
            addColumnIfNotExists(database, "checkin_configs", "metadata", "TEXT DEFAULT ''")
            addColumnIfNotExists(database, "checkin_configs", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
        }
    }
    
    // 从版本6到版本7的迁移 - 创建统一的打卡表并迁移数据
    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建新的unified_checkins表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `unified_checkins` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `moodType` TEXT,
                    `tag` TEXT,
                    `date` TEXT NOT NULL,
                    `count` INTEGER NOT NULL,
                    `note` TEXT,
                    `attachmentUri` TEXT,
                    `duration` INTEGER,
                    `rating` INTEGER,
                    `isCompleted` INTEGER NOT NULL,
                    `configId` INTEGER,
                    `metadata` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """)
            
            // 创建新的unified_checkin_configs表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `unified_checkin_configs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `description` TEXT,
                    `buttonLabel` TEXT NOT NULL,
                    `startDate` TEXT NOT NULL,
                    `targetDate` TEXT,
                    `icon` TEXT NOT NULL,
                    `color` TEXT NOT NULL,
                    `isActive` INTEGER NOT NULL,
                    `targetValue` INTEGER,
                    `reminderTime` TEXT,
                    `isRecurring` INTEGER NOT NULL,
                    `recurrencePattern` TEXT,
                    `metadata` TEXT,
                    `createdAt` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL
                )
            """)
            
            // 将旧的checkins数据迁移到新的unified_checkins表
            database.execSQL("""
                INSERT INTO unified_checkins (
                    name, type, moodType, tag, date, count, note, attachmentUri, 
                    duration, rating, isCompleted, metadata, createdAt, updatedAt
                )
                SELECT 
                    name, type, moodType, tag, date, count, note, attachmentUri, 
                    duration, rating, isCompleted, metadata, createdAt, updatedAt
                FROM checkins
            """)
            
            // 将旧的checkin_configs数据迁移到新的unified_checkin_configs表
            database.execSQL("""
                INSERT INTO unified_checkin_configs (
                    name, type, description, buttonLabel, startDate, targetDate, 
                    icon, color, isActive, targetValue, reminderTime, isRecurring, 
                    recurrencePattern, metadata, createdAt, updatedAt
                )
                SELECT 
                    name, type, description, buttonLabel, startDate, targetDate, 
                    icon, color, isActive, targetValue, reminderTime, isRecurring, 
                    recurrencePattern, metadata, createdAt, updatedAt
                FROM checkin_configs
            """)
            
            // 保留旧表以备回滚，但也可以选择删除它们
            // database.execSQL("DROP TABLE IF EXISTS checkins")
            // database.execSQL("DROP TABLE IF EXISTS checkin_configs")
        }
    }
    
    /**
     * Migration from version 7 to 8 - Add database indexes for performance optimization
     * 
     * This migration adds indexes to frequently queried columns to improve query performance:
     * - unified_checkins: date, type, name, and composite indexes
     * - habits: isActive for filtering active habits
     * - habit_records: composite index on habitId and date for efficient lookups
     * 
     * These indexes are particularly important for:
     * - Date range queries (history, statistics)
     * - Type-based filtering (check-in dashboard)
     * - Name-based lookups (individual habit/check-in history)
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add indexes for UnifiedCheckIn table (primary check-in system)
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_date ON unified_checkins(date)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_type ON unified_checkins(type)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_name ON unified_checkins(name)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_type_date ON unified_checkins(type, date)")
            
            // Add indexes for legacy tables (maintained during transition period)
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_active ON habits(isActive)")
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_habit_records_habit_date ON habit_records(habitId, date)")
        }
    }
    
    /**
     * Migration from version 8 to 9 - Add missing columns to app_config table
     * 
     * This migration adds the required columns to app_config table to match AppConfigEntity:
     * - createdAt: Timestamp when the config was created
     * - updatedAt: Timestamp when the config was last updated
     * - reservedText1: Reserved text field for future use
     * - reservedText2: Reserved text field for future use
     */
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add missing columns to app_config table
            addColumnIfNotExists(database, "app_config", "reservedText1", "TEXT DEFAULT NULL")
            addColumnIfNotExists(database, "app_config", "reservedText2", "TEXT DEFAULT NULL")
            addColumnIfNotExists(database, "app_config", "createdAt", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfNotExists(database, "app_config", "darkMode", "INTEGER DEFAULT NULL")
            addColumnIfNotExists(database, "app_config", "reservedInt1", "INTEGER DEFAULT NULL")
            addColumnIfNotExists(database, "app_config", "reservedInt2", "INTEGER DEFAULT NULL")
            addColumnIfNotExists(database, "app_config", "updatedAt", "INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * Migration from version 9 to 10 - Add new columns to habits table for habit statistics and display type
     *
     * This migration adds the required columns to habits table to support:
     * - displayType: Enum to specify whether to show weekly or monthly view
     * - longestStreak: Longest consecutive days of check-ins
     * - currentStreak: Current consecutive days of check-ins
     * - totalCheckIns: Total number of check-ins for this habit
     */
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add new columns to habits table
            addColumnIfNotExists(database, "habits", "displayType", "TEXT NOT NULL DEFAULT 'WEEKLY'")
            addColumnIfNotExists(database, "habits", "longestStreak", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfNotExists(database, "habits", "currentStreak", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfNotExists(database, "habits", "totalCheckIns", "INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * Migration from version 10 to 11 - Add countdown check-in fields
     *
     * This migration adds the required columns to unified_checkin_configs table to support:
     * - tag: Tag for check-in countdown
     * - countdownMode: Mode of countdown (DAY_COUNTDOWN or CHECKIN_COUNTDOWN)
     * - countdownTarget: Target value for countdown (days or check-in count)
     * - countdownProgress: Current progress for check-in countdown
     */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add countdown-related columns to unified_checkin_configs table
            addColumnIfNotExists(database, "unified_checkin_configs", "tag", "TEXT DEFAULT NULL")
            addColumnIfNotExists(database, "unified_checkin_configs", "countdownMode", "TEXT DEFAULT NULL")
            addColumnIfNotExists(database, "unified_checkin_configs", "countdownTarget", "INTEGER DEFAULT NULL")
            addColumnIfNotExists(database, "unified_checkin_configs", "countdownProgress", "INTEGER NOT NULL DEFAULT 0")
        }
    }
}