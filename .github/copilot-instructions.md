# Love Diary - Copilot Instructions

This document provides context and guidelines for GitHub Copilot when working on the Love Diary repository.

## Project Overview

Love Diary is an Android diary application designed for long-distance relationships, allowing couples to track their daily moods, milestones, and memories. The app is built with modern Android development practices.

## Technology Stack

- **Language**: Kotlin 2.0.21 (99.4% of codebase)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger 2.52
- **Database**: Room 2.6.1 with SQLite (currently at version 8)
- **Async Operations**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil 2.5.0
- **Charts**: Vico Charts 1.12.0
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34
- **Compile SDK**: 34

## Project Structure

```
app/src/main/java/com/love/diary/
├── data/
│   ├── backup/          # Backup/restore managers
│   ├── database/        # Room database, DAOs, entities
│   ├── model/           # Data models and enums
│   └── repository/      # Repository layer
├── di/                  # Dependency injection modules (Hilt)
├── presentation/
│   ├── components/      # Reusable UI components
│   ├── screens/         # Screen composables
│   └── viewmodel/       # ViewModels
├── ui/
│   └── theme/           # Theme configuration
└── util/                # Utility classes
    ├── NotificationHelper.kt  # Local notifications and reminders
    └── ShareHelper.kt         # Content sharing (text and images)
```

## Building and Testing

### Build Commands
```bash
# Build the project
./gradlew assembleDebug

# Build release version
./gradlew assembleRelease
```

### Test Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

## Code Style and Conventions

### Kotlin Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Keep functions small and focused
- Prefer immutability (val over var)
- Use data classes for models
- Leverage Kotlin's null safety features

### Documentation
- Add KDoc comments for all public APIs
- Document repository methods with error handling details
- Document ViewModel state classes
- Include usage examples in utility classes

### Naming Conventions
- ViewModels: `*ViewModel` (e.g., `HomeViewModel`, `HistoryViewModel`)
- Repositories: `*Repository` (e.g., `AppRepository`, `CheckInRepository`)
- Screens: `*Screen` (e.g., `HomeScreen`, `HistoryScreen`)
- Composables: PascalCase (e.g., `MoodCard`, `StatisticsChart`)
- DAOs: `*Dao` (e.g., `MoodDao`, `CheckInDao`)
- Entities: Match table names or model names (e.g., `DailyMood`, `UnifiedCheckIn`)

### Compose Guidelines
- Use Material 3 components
- Prefer `@Composable` functions over classes
- Extract reusable components to `presentation/components/`
- Use `remember` and `rememberSaveable` appropriately
- Follow unidirectional data flow pattern
- Pass ViewModels to screen-level composables only

## Architecture Patterns

### MVVM Implementation
- **View**: Jetpack Compose UI
- **ViewModel**: Business logic, state management with StateFlow
- **Model**: Data layer (Repository + Room)

### State Management
- Use `StateFlow` for observable state in ViewModels
- Create UI state data classes for each screen
- Use `collectAsState()` in Compose for observing flows

### Repository Pattern
- Repositories abstract data sources
- Use Flow for reactive queries
- Use suspend functions for one-shot operations
- Handle errors with try-catch and logging

### Dependency Injection
- Use Hilt for dependency injection
- Module classes in `di/` package
- `@HiltViewModel` for ViewModels
- `@Singleton` for repositories

## Database

### Room Configuration
- Database version: 8
- Schema location: `app/schemas/` (configured via KSP in app build.gradle.kts)
- Use Type Converters for complex types (see `Converters.kt`)
- Add database migrations for schema changes (see `MigrationHelper.kt`)

### Database Entities
Key tables:
- `daily_mood` - Daily mood tracking
- `unified_check_in` - Primary unified check-in system
- `habit` - Legacy habit system (being phased out)
- `event` - Event timeline entries

### Queries
- Use Flow for reactive queries that need to observe changes
- Use suspend functions for one-time queries
- Add appropriate indexes for frequently queried columns
- Follow Room best practices for complex queries

## Testing Strategy

### Unit Tests
- Test ViewModels with coroutine testing
- Test repositories with test doubles
- Mock dependencies using Mockito
- Use `kotlinx-coroutines-test` for testing coroutines

### Test Location
- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`

### Testing Guidelines
- Write tests for business logic
- Test error handling paths
- Verify state changes in ViewModels
- Test database operations with Room testing utilities

## Key Features to Maintain

### Core Features
1. Daily mood tracking with 6 emotion types
2. Relationship day counter
3. Anniversary reminders
4. Habit tracking
5. Statistics and trends visualization
6. Data backup/restore functionality

### Privacy & Security
- All data stored locally by default
- No analytics or tracking
- Secure file sharing with FileProvider
- Support for encrypted database (ready)

### Accessibility
- Content descriptions for all interactive elements
- Screen reader support
- High contrast color schemes
- Proper touch target sizes (48dp minimum)

## Common Tasks

### Adding a New Screen
1. Create screen composable in `presentation/screens/[feature]/`
2. Create ViewModel in `presentation/viewmodel/`
3. Add navigation route in navigation configuration
4. Update NavHost with new destination
5. Add necessary repository methods if needed

### Adding a New Database Entity
1. Create entity class in `data/database/entities/`
2. Create DAO interface in `data/database/dao/`
3. Update `LoveDatabase` with new entities and increment version
4. Create migration in `MigrationHelper.kt`
5. Build project to export schema to `app/schemas/` (automatic via KSP)
6. Update repository if needed

### Adding a New Dependency
1. Add to `app/build.gradle.kts`
2. Sync Gradle
3. If adding Hilt module, create in `di/` package

## Important Notes

### Migration Strategy
The project is migrating from a legacy Habit system to a unified UnifiedCheckIn system:
- **UnifiedCheckIn**: Primary data store (preferred for new features)
- **Legacy Habit**: Maintained for backward compatibility
- Use bridge pattern during transition
- See `docs/REFACTORING_PLAN.md` for details

### Documentation
Comprehensive docs available in `docs/`:
- `ARCHITECTURE.md` - Complete architecture guide
- `DATABASE_SCHEMA.md` - Database design and schema
- `REFACTORING_PLAN.md` - Refactoring strategy and roadmap

## Git Workflow

- Use feature branches for new work
- Write semantic commit messages
- Keep pull requests focused and small
- Maintain linear history where possible

## Additional Resources

- Material Design 3: https://m3.material.io/
- Jetpack Compose: https://developer.android.com/jetpack/compose
- Kotlin Coroutines: https://kotlinlang.org/docs/coroutines-overview.html
- Room Database: https://developer.android.com/training/data-storage/room
