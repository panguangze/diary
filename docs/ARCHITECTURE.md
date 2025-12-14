# Love Diary - Architecture Documentation

## Overview

Love Diary is an Android application designed for couples in long-distance relationships to track their daily moods, habits, milestones, and memories. The app follows MVVM architecture with clean separation of concerns.

## Technology Stack

- **Language**: Kotlin 2.0.21 (99.4% of codebase)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger
- **Database**: Room with SQLite
- **Async**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Charts**: Vico Charts
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34

## Project Structure

```
app/src/main/java/com/love/diary/
├── data/
│   ├── backup/              # Backup and restore managers
│   ├── database/            # Room database, DAOs, entities
│   │   ├── dao/            # Data Access Objects
│   │   ├── entities/       # Room entities
│   │   ├── LoveDatabase.kt # Database definition
│   │   ├── Converters.kt   # Type converters
│   │   └── MigrationHelper.kt # Database migrations
│   ├── model/              # Data models and enums
│   └── repository/         # Repository layer
├── di/                     # Dependency injection modules
├── habit/                  # Habit feature (legacy)
├── navigation/             # Navigation configuration
├── presentation/
│   ├── components/         # Reusable UI components
│   ├── screens/           # Screen composables
│   │   ├── home/
│   │   ├── history/
│   │   ├── settings/
│   │   ├── setup/
│   │   └── statistics/
│   └── viewmodel/         # ViewModels
├── ui/
│   └── theme/             # Theme configuration
├── util/                  # Utility classes
├── LoveDiaryApp.kt       # Application class
└── MainActivity.kt        # Main activity
```

## Architecture Layers

### 1. Presentation Layer

#### ViewModels
- **HomeViewModel**: Manages home screen state, mood selection, day counting
- **HistoryViewModel**: Handles mood history loading and filtering
- **StatisticsViewModel**: Computes mood statistics and trends
- **SettingsViewModel**: Manages app configuration
- **CheckInViewModel**: Handles unified check-in operations
- **HabitViewModel**: Legacy habit management

**Key Patterns**:
- State management with `StateFlow`
- UI state classes for reactive updates
- Coroutine-based async operations

#### Screens (Compose)
- **HomeScreen**: Daily mood tracking and relationship day counter
- **HabitListScreen**: Habit tracking and check-ins
- **HistoryScreen**: View past mood entries
- **StatisticsScreen**: Visualize mood trends
- **SettingsScreen**: App configuration
- **FirstRunScreen**: Initial setup flow
- **CheckInDashboardScreen**: Unified check-in interface (not yet in navigation)

### 2. Domain Layer

#### Models
- **MoodType**: Enum for mood categories (HAPPY, SATISFIED, NORMAL, SAD, ANGRY, OTHER)
- **HabitType**: Enum for habit types (POSITIVE, COUNTDOWN)
- **CheckInType**: Enum for unified check-in types (12 types including LOVE_DIARY, HABIT, EXERCISE, etc.)
- **EventType**: Legacy enum (MOOD_DIARY, HABIT_CHECK_IN)

#### Entities
See `DATABASE_SCHEMA.md` for complete entity documentation.

### 3. Data Layer

#### Repositories
- **AppRepository**: Main repository for app config and daily moods
- **CheckInRepository**: Unified check-in operations and configuration
- **HabitRepository**: Legacy habit operations (being phased out)

**Key Features**:
- Flow-based reactive queries
- Suspend functions for one-shot operations
- Automatic config creation for new check-in types
- Transaction handling for data consistency

#### DAOs
- **AppConfigDao**: App configuration CRUD
- **DailyMoodDao**: Daily mood entries
- **UnifiedCheckInDao**: Unified check-in system (primary)
- **HabitDao**: Legacy habit operations
- **EventDao**: Legacy event operations

### 4. Dependency Injection

**Modules**:
- **DatabaseModule**: Provides Room database and DAOs
- **RepositoryModule**: Provides repository instances
- **BackupModule**: Provides backup/restore managers

**Scope**: Application-level singletons with `@Singleton` annotation

## Key Features Implementation

### 1. Daily Mood Tracking

**Flow**:
1. User selects mood on HomeScreen
2. HomeViewModel calls AppRepository
3. Repository creates UnifiedCheckIn entry with type=LOVE_DIARY
4. DailyMoodEntity created for backward compatibility
5. UI updates via StateFlow

**Key Classes**:
- `HomeScreen.kt`
- `HomeViewModel.kt`
- `AppRepository.kt`
- `DailyMoodEntity.kt`

### 2. Habit Tracking

**Current Implementation** (Legacy):
- Uses `Habit` and `HabitRecord` entities
- `HabitRepository` for data operations
- `HabitListScreen` for UI

**Transitioning To**:
- UnifiedCheckIn with type=HABIT
- CheckInRepository for operations
- Integration with unified system

### 3. Unified Check-In System

**Design**:
- Single `UnifiedCheckIn` entity for all check-in types
- Flexible field system (duration, rating, count, etc.)
- Metadata field for extensibility
- Configuration via `UnifiedCheckInConfig`

**Supported Types**:
- LOVE_DIARY: Relationship mood tracking
- HABIT: General habits
- EXERCISE, WORKOUT: Fitness tracking
- STUDY, READING: Learning activities
- DIET, WATER: Health tracking
- MEDITATION, SLEEP: Wellness
- MILESTONE: Special events
- CUSTOM: User-defined

**Benefits**:
- Single database table reduces complexity
- Consistent API across check-in types
- Easy to add new check-in categories
- Unified statistics and trends

### 4. Statistics & Trends

**Implementation**:
- `StatisticsViewModel` computes aggregations
- `StatisticsScreen` displays charts with Vico
- Query patterns in DAOs for efficient aggregation
- Support for date range filtering

### 5. Data Backup & Restore

**Implementation**:
- `DataBackupManager` handles JSON serialization
- Exports all entities to single JSON file
- Restore with version compatibility checks
- Uses SAF (Storage Access Framework) for file access

### 6. Notifications

**Implementation**:
- `NotificationHelper` manages local notifications
- Daily reminder support
- Anniversary notifications
- Permission handling for Android 13+

## Data Flow

### Typical Read Flow
```
Screen (Compose)
    ↓
ViewModel (observes StateFlow)
    ↓
Repository (provides Flow)
    ↓
DAO (Room query)
    ↓
SQLite Database
```

### Typical Write Flow
```
Screen (user action)
    ↓
ViewModel (calls suspend function)
    ↓
Repository (business logic)
    ↓
DAO (insert/update)
    ↓
SQLite Database
    ↓
Flow emits new data
    ↓
ViewModel updates StateFlow
    ↓
Screen recomposes
```

## State Management

### UI State Pattern
```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val dayIndex: Int = 0,
    val selectedMood: MoodType? = null,
    val error: String? = null,
    // ... other state
)
```

**Benefits**:
- Single source of truth
- Immutable state
- Easy to test
- Clear state transitions

### StateFlow Usage
- ViewModels expose `StateFlow<UiState>`
- Screens collect with `collectAsState()`
- Updates via `MutableStateFlow.update { }`

## Error Handling

**Strategy**:
- Try-catch in repository layer
- Error states in UI state classes
- User-friendly error messages
- Logging for debugging

## Testing Strategy

### Unit Tests
- ViewModel tests with coroutine testing
- Repository tests with test doubles
- Mock dependencies with Mockito

**Example**: `HomeViewModelTest.kt` tests mood selection and state updates

### Instrumented Tests
- UI tests for critical flows (future)
- Database migration tests (future)

## Performance Considerations

### Database
- Indexes on frequently queried columns (to be added)
- Pagination for large datasets
- Efficient query patterns in DAOs

### UI
- LazyColumn for scrolling lists
- Coil for efficient image loading
- Compose performance best practices

### Memory
- Flow cancellation on ViewModel clear
- Proper lifecycle handling
- No memory leaks from coroutines

## Design Patterns

### Repository Pattern
- Abstracts data source
- Single source of truth
- Mediates between DAOs and ViewModels

### Observer Pattern
- Flow for reactive data
- StateFlow for UI state
- LiveData not used (Compose-first)

### Dependency Injection
- Constructor injection
- Hilt for automatic DI
- Testable architecture

### MVVM
- Clear separation of concerns
- ViewModels don't reference Views
- UI state driven by ViewModel

## Security & Privacy

### Data Protection
- All data stored locally
- No cloud sync (by default)
- Backup encryption ready (future)
- FileProvider for secure sharing

### Permissions
- `POST_NOTIFICATIONS` for reminders (Android 13+)
- File access via SAF
- No unnecessary permissions

## Future Improvements

### Short-term
1. Add database indexes for performance
2. Complete migration from Habit to UnifiedCheckIn
3. Integrate CheckInDashboardScreen into navigation
4. Add more comprehensive tests
5. Improve error handling and logging

### Medium-term
1. Migrate legacy tables to unified system
2. Add cloud sync (optional)
3. Implement backup encryption
4. Add widget support
5. Enhance statistics with more insights

### Long-term
1. Simplify database schema
2. Add multi-language support
3. Implement sharing between partners
4. Add photo album feature
5. Timeline visualization

## Troubleshooting

### Common Issues

**Database migrations fail**:
- Check MigrationHelper.kt
- Ensure all migrations are added to Room builder
- Test migrations in instrumented tests

**ViewModels not retaining state**:
- Ensure proper lifecycle scope
- Check SavedStateHandle usage
- Verify Hilt injection

**UI not updating**:
- Verify Flow collection in Compose
- Check StateFlow updates
- Ensure proper recomposition triggers

## Contributing Guidelines

1. Follow Kotlin coding conventions
2. Add KDoc for public APIs
3. Update tests for new features
4. Run linter before committing
5. Keep functions small and focused
6. Use meaningful variable names

## See Also

- `DATABASE_SCHEMA.md` - Database design documentation
- `README.md` - Project overview and setup
- Test files for implementation examples
