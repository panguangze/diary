package com.love.diary.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.love.diary.data.database.LoveDatabase
import com.love.diary.data.database.MigrationHelper
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test for database migrations.
 * 
 * Tests that database migrations work correctly without data loss.
 * These tests run on an Android device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LoveDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )
    
    /**
     * Test migration from version 7 to 8.
     * 
     * Verifies that:
     * 1. Migration completes successfully
     * 2. All expected indexes are created
     * 3. Existing data is preserved
     * 4. Database schema is valid
     */
    @Test
    @Throws(IOException::class)
    fun migrate7To8() {
        // Create database at version 7
        var db = helper.createDatabase(TEST_DB, 7).apply {
            // Insert some test data into version 7 schema
            execSQL("""
                INSERT INTO unified_checkins 
                (name, type, date, count, isCompleted, createdAt, updatedAt) 
                VALUES 
                ('Test Habit', 'HABIT', '2024-01-01', 1, 1, 1704067200000, 1704067200000)
            """)
            
            execSQL("""
                INSERT INTO habits 
                (name, buttonLabel, type, startDate, currentCount, isCompletedToday, isActive, color, icon, tags, createdAt, updatedAt) 
                VALUES 
                ('Morning Exercise', '打卡', 'POSITIVE', '2024-01-01', 5, 0, 1, '#6200EE', '🏃', 'fitness,morning', 1704067200000, 1704067200000)
            """)
            
            close()
        }
        
        // Re-open the database with version 8 and provide MIGRATION_7_8
        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, MigrationHelper.MIGRATION_7_8)
        
        // Verify indexes were created by attempting to use them in queries
        val cursor = db.query("""
            SELECT * FROM unified_checkins 
            WHERE date = '2024-01-01' AND type = 'HABIT'
        """)
        
        // Verify data was preserved
        assert(cursor.count > 0) { "Data should be preserved after migration" }
        cursor.moveToFirst()
        val nameColumnIndex = cursor.getColumnIndex("name")
        assert(nameColumnIndex >= 0) { "name column should exist" }
        val name = cursor.getString(nameColumnIndex)
        assert(name == "Test Habit") { "Data values should be preserved" }
        cursor.close()
        
        // Verify habits table data preserved
        val habitsCursor = db.query("SELECT * FROM habits WHERE name = 'Morning Exercise'")
        assert(habitsCursor.count > 0) { "Habits data should be preserved" }
        habitsCursor.close()
        
        // Verify indexes exist by checking sqlite_master
        val indexCursor = db.query("""
            SELECT name FROM sqlite_master 
            WHERE type = 'index' 
            AND name IN (
                'idx_unified_checkins_date',
                'idx_unified_checkins_type',
                'idx_unified_checkins_name',
                'idx_unified_checkins_type_date',
                'idx_habits_active',
                'idx_habit_records_habit_date'
            )
        """)
        
        val expectedIndexes = setOf(
            "idx_unified_checkins_date",
            "idx_unified_checkins_type",
            "idx_unified_checkins_name",
            "idx_unified_checkins_type_date",
            "idx_habits_active",
            "idx_habit_records_habit_date"
        )
        
        val foundIndexes = mutableSetOf<String>()
        while (indexCursor.moveToNext()) {
            val indexNameColumnIndex = indexCursor.getColumnIndex("name")
            if (indexNameColumnIndex >= 0) {
                foundIndexes.add(indexCursor.getString(indexNameColumnIndex))
            }
        }
        indexCursor.close()
        
        assert(foundIndexes.containsAll(expectedIndexes)) { 
            "All expected indexes should be created. Found: $foundIndexes, Expected: $expectedIndexes" 
        }
        
        db.close()
    }
    
    /**
     * Test full migration path from version 4 to 8.
     * 
     * Verifies that sequential migrations work correctly.
     */
    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create database at version 4
        helper.createDatabase(TEST_DB, 4).apply {
            // Version 4 had basic schema
            close()
        }
        
        // Run all migrations up to version 8
        val db = helper.runMigrationsAndValidate(
            TEST_DB, 
            8, 
            true, 
            MigrationHelper.MIGRATION_4_5,
            MigrationHelper.MIGRATION_5_6,
            MigrationHelper.MIGRATION_6_7,
            MigrationHelper.MIGRATION_7_8
        )
        
        // Verify final schema is valid
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableSetOf<String>()
        while (cursor.moveToNext()) {
            val nameColumnIndex = cursor.getColumnIndex("name")
            if (nameColumnIndex >= 0) {
                tables.add(cursor.getString(nameColumnIndex))
            }
        }
        cursor.close()
        
        // Verify all expected tables exist
        val expectedTables = setOf(
            "app_config",
            "daily_mood",
            "habits",
            "habit_records",
            "events",
            "event_configs",
            "unified_checkins",
            "unified_checkin_configs"
        )
        
        assert(tables.containsAll(expectedTables)) { 
            "All expected tables should exist. Found: $tables" 
        }
        
        db.close()
    }
    
    /**
     * Test that database can be created from scratch at version 8.
     */
    @Test
    fun createDatabaseVersion8() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.databaseBuilder(
            context,
            LoveDatabase::class.java,
            "test-db-v8"
        )
            .addMigrations(
                MigrationHelper.MIGRATION_4_5,
                MigrationHelper.MIGRATION_5_6,
                MigrationHelper.MIGRATION_6_7,
                MigrationHelper.MIGRATION_7_8
            )
            .build()
        
        // Verify database is created and accessible
        val appConfigDao = db.appConfigDao()
        assert(appConfigDao != null) { "Database should be created successfully" }
        
        db.close()
        context.deleteDatabase("test-db-v8")
    }
}
