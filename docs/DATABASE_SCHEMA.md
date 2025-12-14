# Database Schema Documentation

## Overview
The Love Diary application uses a Room database with SQLite. The current schema (version 8) contains entities for tracking mood/diary entries, habits, and unified check-ins.

## Database Version History
- **Version 1-3**: Initial schema with basic mood tracking
- **Version 4**: Added basic daily mood tracking
- **Version 5**: Added Events and EventConfig tables (Migration 4→5)
- **Version 6**: Enhanced check-in tables with additional fields (Migration 5→6)
- **Version 7**: Introduced UnifiedCheckIn system (Migration 6→7)
- **Version 8**: Added performance indexes for optimization (Migration 7→8)

## Core Entities

### 1. AppConfigEntity
**Table Name**: `app_config`

Stores application configuration and user preferences.

**Fields**:
- `id` (PK): Always 1 (singleton)
- `startDate`: Relationship start date (yyyy-MM-dd)
- `startTimeMinutes`: Start time in minutes from midnight
- `coupleName`: Display name for the couple
- `partnerNickname`: Nickname for partner
- `showMoodTip`: Show mood feedback tips
- `showStreak`: Show streak counter
- `showAnniversary`: Show anniversary notifications
- `darkMode`: Dark mode setting (null=system, true=dark, false=light)
- `createdAt`, `updatedAt`: Timestamps

---

### 2. DailyMoodEntity
**Table Name**: `daily_mood`

Stores daily mood entries for the love diary feature.

**Fields**:
- `id` (PK): Auto-generated
- `date`: Entry date (yyyy-MM-dd), unique
- `dayIndex`: Day number in the relationship
- `moodTypeCode`: Mood type code (HAPPY, SAD, etc.)
- `moodScore`: Numerical mood score
- `moodText`: Optional mood description
- `hasText`: Boolean indicating if text exists
- `singleImageUri`: URI to attached image
- `isAnniversary`: Whether this day is an anniversary
- `createdAt`, `updatedAt`: Timestamps

**Indexes**: 
- Unique index on `date`
- Index on `dayIndex`
- Index on `attachmentGroupId`

---

### 3. UnifiedCheckIn (Current Primary System)
**Table Name**: `unified_checkins`

**Primary system for all check-in types**. Consolidates functionality from multiple check-in systems.

**Fields**:
- `id` (PK): Auto-generated
- `name`: Check-in name
- `type`: CheckInType (LOVE_DIARY, HABIT, EXERCISE, STUDY, WORKOUT, DIET, MEDITATION, READING, WATER, SLEEP, MILESTONE, CUSTOM)
- `moodType`: Optional mood type
- `tag`: Optional tag/category
- `date`: Check-in date
- `count`: Count/quantity
- `note`: Optional note
- `attachmentUri`: URI to attachment
- `duration`: Duration in minutes
- `rating`: Rating (1-5 stars)
- `isCompleted`: Whether completed
- `configId`: Reference to config
- `metadata`: JSON metadata
- `createdAt`, `updatedAt`: Timestamps

---

### 4. UnifiedCheckInConfig (Current Primary Configuration)
**Table Name**: `unified_checkin_configs`

**Primary configuration system** for check-in types.

**Fields**:
- `id` (PK): Auto-generated
- `name`: Config name
- `type`: CheckInType
- `description`: Optional description
- `buttonLabel`: Button label (default: "打卡")
- `startDate`: Start date
- `targetDate`: Optional target date
- `icon`: Emoji icon (default: "🎯")
- `color`: Color code (default: "#6200EE")
- `isActive`: Whether active
- `targetValue`: Optional target value
- `reminderTime`: Reminder time (HH:mm)
- `isRecurring`: Whether recurring
- `recurrencePattern`: Recurrence pattern
- `metadata`: JSON metadata
- `createdAt`, `updatedAt`: Timestamps

---

### Legacy Entities (Maintained for Compatibility)

#### Habit
**Table Name**: `habits`

**Status**: Legacy - Being phased out in favor of UnifiedCheckIn.

Used for habit tracking. New code should use UnifiedCheckIn with type=HABIT.

#### HabitRecord
**Table Name**: `habit_records`

**Status**: Legacy - Individual habit check-in records.

New code should use UnifiedCheckIn for storage.

#### Event
**Table Name**: `events`

**Status**: Legacy - General event tracking.

Overlaps with UnifiedCheckIn functionality.

#### EventConfig
**Table Name**: `event_configs`

**Status**: Legacy - Event configuration.

Overlaps with UnifiedCheckInConfig.

---

## Architecture Recommendations

### Current State
The database evolved with three overlapping systems:
1. Habit/HabitRecord (original)
2. Event/EventConfig (first unification attempt)
3. UnifiedCheckIn/UnifiedCheckInConfig (current unified system)

### Recommended Approach

**For New Features**:
- Use `UnifiedCheckIn` and `UnifiedCheckInConfig`
- All new check-in types should use the unified system

**For Existing Code**:
- Legacy tables maintained for backward compatibility
- Gradual migration to unified system
- No breaking changes to existing data

### Performance Optimization

Recommended indexes to add in future migration:
```sql
CREATE INDEX idx_unified_checkins_date ON unified_checkins(date);
CREATE INDEX idx_unified_checkins_type ON unified_checkins(type);
CREATE INDEX idx_unified_checkins_name ON unified_checkins(name);
CREATE INDEX idx_unified_checkins_type_date ON unified_checkins(type, date);
```

## Repository Layer

### CheckInRepository
Primary repository for UnifiedCheckIn system. Handles:
- All check-in operations
- Configuration management
- Statistics and trends
- Date range queries

### HabitRepository
Legacy repository for Habit system. Currently maintained for:
- Backward compatibility
- Existing habit data
- Migration path to UnifiedCheckIn

### AppRepository
Main repository for:
- App configuration
- Daily mood entries
- General app state

## Migration Strategy

### Short-term (Current)
- Keep all tables for compatibility
- Use UnifiedCheckIn for new features
- Document legacy status

### Long-term (Future)
- Migrate data from legacy to unified
- Add performance indexes
- Deprecate legacy tables
- Simplify to core entities

## See Also

- `MigrationHelper.kt` - Migration implementations
- `LoveDatabase.kt` - Database definition
- `Converters.kt` - Type converters
