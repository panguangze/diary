# Refactoring Plan - Love Diary App

## Executive Summary

This document outlines the refactoring plan for the Love Diary Android application to address database design inconsistencies, unify check-in systems, improve code maintainability, and enhance user experience.

## Current State Analysis

### Strengths
✅ Well-structured MVVM architecture
✅ Modern tech stack (Kotlin 2.0, Jetpack Compose, Material 3)
✅ Comprehensive feature set
✅ Good separation of concerns
✅ Dependency injection with Hilt

### Issues Identified

#### 1. Database Schema Inconsistency
**Problem**: Three overlapping entity systems
- `Habit` + `HabitRecord` (original system)
- `Event` + `EventConfig` (first unification attempt)
- `UnifiedCheckIn` + `UnifiedCheckInConfig` (latest system)

**Impact**:
- Confusion for developers
- Duplicated code
- Inconsistent data storage
- Difficult to maintain

#### 2. Incomplete Feature Integration
**Problem**: 
- `CheckInDashboardScreen` exists but not in navigation
- Unified check-in system not fully adopted
- `HabitListScreen` still uses legacy `Habit` tables

**Impact**:
- Fragmented user experience
- Incomplete feature implementation
- Technical debt accumulation

#### 3. Missing Performance Optimizations
**Problem**:
- No indexes on frequently queried columns
- Potential performance issues with large datasets

**Impact**:
- Slow queries as data grows
- Poor user experience with lag

#### 4. Limited Documentation
**Problem**:
- No architecture documentation
- No database schema documentation
- Hard for new contributors

**Impact**:
- Difficult onboarding
- Maintenance challenges
- Risk of inconsistent implementations

## Refactoring Goals

### Primary Goals
1. ✅ **Document existing architecture** (COMPLETED)
2. **Unify check-in systems** using UnifiedCheckIn as primary
3. **Improve code maintainability** through better structure
4. **Enhance user experience** with consistent UI/UX
5. **Optimize performance** with proper indexing

### Secondary Goals
- Add comprehensive tests
- Improve error handling
- Enhance backup/restore functionality
- Add missing features

## Implementation Plan

### Phase 1: Documentation & Planning ✅ (COMPLETED)

**Deliverables**:
- [x] Architecture documentation (`ARCHITECTURE.md`)
- [x] Database schema documentation (`DATABASE_SCHEMA.md`)
- [x] Refactoring plan (`REFACTORING_PLAN.md`)

**Status**: COMPLETED

---

### Phase 2: Database Optimization (RECOMMENDED)

**Goal**: Add performance indexes without breaking changes

**Tasks**:
1. Create Migration 7→8 with indexes
2. Add indexes for common query patterns
3. Test migration thoroughly

**Implementation**:

```kotlin
// In MigrationHelper.kt
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add indexes for UnifiedCheckIn
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_date ON unified_checkins(date)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_type ON unified_checkins(type)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_name ON unified_checkins(name)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_unified_checkins_type_date ON unified_checkins(type, date)")
        
        // Add indexes for legacy tables (for migration period)
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_habits_active ON habits(isActive)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_habit_records_habit_date ON habit_records(habitId, date)")
    }
}
```

**Files to modify**:
- `MigrationHelper.kt` - Add migration
- `LoveDatabase.kt` - Update version to 8, add migration

**Testing**:
- Test migration on clean install
- Test migration from version 7
- Verify index creation
- Measure query performance improvement

**Risk**: Low - Adding indexes is non-breaking

**Priority**: High - Improves performance immediately

---

### Phase 3: Bridge HabitRepository to UnifiedCheckIn (RECOMMENDED)

**Goal**: Make HabitRepository use UnifiedCheckIn internally while keeping API compatible

**Approach**: Create compatibility layer

**Tasks**:
1. Update HabitRepository to write to both systems
2. Read from UnifiedCheckIn primarily, fallback to Habit
3. Mark legacy methods as deprecated
4. Add migration utility methods

**Implementation Strategy**:

```kotlin
class DefaultHabitRepository(private val database: LoveDatabase) : HabitRepository {
    
    override suspend fun checkInHabit(habitId: Long, tag: String?): Boolean {
        val habit = database.habitDao().getHabitById(habitId) ?: return false
        
        // Write to BOTH systems during transition
        // 1. Write to UnifiedCheckIn (new system)
        val checkInId = database.unifiedCheckInDao().insertCheckIn(
            UnifiedCheckIn(
                name = habit.name,
                type = CheckInType.HABIT,
                tag = tag,
                date = LocalDate.now().toString(),
                count = habit.currentCount + 1,
                note = tag,
                isCompleted = true
            )
        )
        
        // 2. Write to legacy HabitRecord (for compatibility)
        val record = HabitRecord(
            habitId = habitId,
            count = habit.currentCount + 1,
            note = tag,
            date = LocalDate.now().toString()
        )
        database.habitDao().insertHabitRecord(record)
        
        // 3. Update habit
        val updatedHabit = habit.copy(
            currentCount = habit.currentCount + 1,
            isCompletedToday = true,
            updatedAt = System.currentTimeMillis()
        )
        database.habitDao().updateHabit(updatedHabit)
        
        return checkInId > 0
    }
    
    // Read from UnifiedCheckIn with fallback
    suspend fun getCheckInHistory(habitId: Long): List<UnifiedCheckIn> {
        val habit = database.habitDao().getHabitById(habitId) ?: return emptyList()
        return database.unifiedCheckInDao()
            .getCheckInsByName(habit.name)
            .first()
    }
}
```

**Files to modify**:
- `HabitRepository.kt` - Update implementation

**Benefits**:
- Gradual migration
- No breaking changes
- Data written to both systems
- Can switch read source later

**Testing**:
- Test check-in creates both records
- Test read operations
- Test backward compatibility

**Risk**: Low - Maintains compatibility

**Priority**: High - Enables unified system adoption

---

### Phase 4: Update HabitListScreen (RECOMMENDED)

**Goal**: Update UI to show UnifiedCheckIn data while keeping visual design

**Tasks**:
1. Update ViewModel to use CheckInRepository
2. Map UnifiedCheckIn data to UI state
3. Keep existing UI components
4. Add migration notice (optional)

**Implementation**:

Option A: Dual-mode (show both systems)
Option B: Unified-only (simpler, recommended)

**Files to modify**:
- `HabitViewModel.kt` - Update data source
- `HabitListScreen.kt` - Update to handle UnifiedCheckIn

**Testing**:
- UI tests for check-in flow
- Verify data display
- Test tag functionality

**Risk**: Medium - UI changes need testing

**Priority**: Medium - Completes unified system

---

### Phase 5: Add Missing Navigation (LOW PRIORITY)

**Goal**: Integrate CheckInDashboardScreen into app navigation

**Decision**: This screen may not be needed if HabitListScreen serves the purpose

**Recommendation**: 
- Keep CheckInDashboardScreen as alternative view
- Don't add to main navigation yet
- Consider consolidating with HabitListScreen later

**Priority**: Low - Not critical for functionality

---

### Phase 6: Code Quality Improvements (ONGOING)

**Tasks**:
1. Add KDoc comments to public APIs
2. Add error handling in repositories
3. Add comprehensive logging
4. Refactor large functions
5. Extract magic numbers to constants

**Files to review**:
- All ViewModels
- All Repositories
- Screen composables

**Priority**: Ongoing - Do incrementally

---

### Phase 7: Testing Enhancements (FUTURE)

**Tasks**:
1. Add repository tests
2. Add migration tests
3. Add UI tests for critical flows
4. Add integration tests

**Priority**: Low - Future work

---

## Implementation Priority

### High Priority (Do Now)
1. ✅ Documentation (DONE)
2. Database indexes (Phase 2)
3. HabitRepository bridge (Phase 3)

### Medium Priority (Next Sprint)
4. Update HabitListScreen (Phase 4)
5. Code quality improvements (Phase 6)

### Low Priority (Future)
6. Navigation changes (Phase 5)
7. Testing enhancements (Phase 7)

## Minimal Change Approach

### What We're NOT Doing (To Minimize Changes)
❌ Not removing legacy tables (data preservation)
❌ Not forcing data migration (gradual transition)
❌ Not rewriting entire screens (incremental updates)
❌ Not changing public APIs (backward compatibility)

### What We ARE Doing (Minimal Impact)
✅ Adding documentation
✅ Adding database indexes (performance)
✅ Creating compatibility layer (bridge pattern)
✅ Updating internal implementations (not APIs)

## Risk Assessment

### Low Risk Changes
- Adding documentation
- Adding database indexes
- Adding new migration

### Medium Risk Changes
- Updating HabitRepository internals
- UI updates to HabitListScreen

### High Risk Changes
- Removing legacy tables (NOT DOING)
- Forcing data migration (NOT DOING)

## Testing Strategy

### Unit Tests
- Repository layer tests
- ViewModel tests
- Migration tests

### Integration Tests
- Database migration tests
- End-to-end check-in flow tests

### Manual Testing
- Fresh install
- Migration from old version
- Check-in operations
- Data integrity verification

## Rollback Plan

### If Phase 2 Fails (Indexes)
- Revert migration
- Keep version 7
- No data loss

### If Phase 3 Fails (Repository Bridge)
- Revert code changes
- Fall back to legacy system
- No data loss (writes to both)

## Success Criteria

### Phase 2 Success
- [ ] Migration 7→8 succeeds
- [ ] Indexes created correctly
- [ ] Query performance improves
- [ ] No data corruption

### Phase 3 Success
- [ ] Check-ins write to both systems
- [ ] Data consistent across systems
- [ ] No breaking changes to API
- [ ] Tests pass

### Phase 4 Success
- [ ] UI displays UnifiedCheckIn data
- [ ] Check-in flow works correctly
- [ ] No UX regression
- [ ] Performance acceptable

## Timeline Estimate

- Phase 1 (Documentation): ✅ DONE
- Phase 2 (Indexes): 1-2 hours
- Phase 3 (Repository Bridge): 2-3 hours
- Phase 4 (UI Updates): 3-4 hours
- Phase 5 (Navigation): 1-2 hours (optional)
- Phase 6 (Code Quality): Ongoing
- Phase 7 (Testing): Future work

**Total for High Priority**: 3-5 hours

## Maintenance Plan

### Short-term (1-3 months)
- Monitor dual-write system
- Verify data consistency
- Fix any edge cases
- Gather user feedback

### Medium-term (3-6 months)
- Analyze usage patterns
- Decide on legacy table deprecation
- Plan data migration
- Add more tests

### Long-term (6+ months)
- Remove legacy tables
- Simplify architecture
- Add new features on unified system

## Conclusion

This refactoring plan takes a **minimal, incremental approach** to:
1. ✅ Document the existing architecture
2. Improve database performance with indexes
3. Bridge old and new systems gradually
4. Maintain backward compatibility
5. Enable future improvements

The approach minimizes risk while maximizing value through better documentation, performance, and maintainability.

## Next Steps

1. ✅ Review and approve documentation
2. Implement Phase 2 (Database indexes)
3. Implement Phase 3 (Repository bridge)
4. Test thoroughly
5. Deploy and monitor
6. Gather feedback
7. Continue with Phase 4

## See Also

- `ARCHITECTURE.md` - Architecture documentation
- `DATABASE_SCHEMA.md` - Database schema documentation
- `README.md` - Project overview
