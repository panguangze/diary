# Love Diary - 异地恋日记 📱❤️

An Android diary application designed for long-distance relationships, allowing couples to track their daily moods, milestones, and memories.

## 📋 Features

### Core Functionality
- **Daily Mood Tracking**: Record daily feelings with 6 emotion types (Happy, Satisfied, Normal, Sad, Angry, Other)
- **Relationship Timeline**: Automatic day counter from relationship start date
- **Anniversary Reminders**: Special notifications for 100-day milestones and other significant dates
- **Habit Tracking**: Track and monitor daily habits and check-ins
- **Statistics Dashboard**: Visualize mood trends and patterns over time
- **History View**: Browse all past mood entries with search and filter

### User Experience Features
- **Dark Mode**: Full dark theme support with system-follow option
- **Smooth Animations**: Beautiful transitions and micro-interactions
- **Accessibility**: Content descriptions for screen readers
- **Personalization**: Customizable couple name and nicknames

### Data Management
- **Backup & Restore**: Export/import all data as JSON
- **Data Security**: Local SQLite database with Room
- **Privacy First**: All data stored locally on device

### Sharing & Notifications
- **Content Sharing**: Share mood cards as text or beautiful images
- **Daily Reminders**: Optional notification reminders for mood tracking
- **Anniversary Alerts**: Automatic notifications for special milestones

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin (99.4%)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt/Dagger
- **Database**: Room with SQLite
- **Async Operations**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Charts**: Vico Charts

### Project Structure
```
app/
├── data/
│   ├── backup/          # Backup/restore managers
│   ├── database/        # Room database, DAOs, entities
│   ├── model/           # Data models
│   └── repository/      # Repository layer
├── di/                  # Dependency injection modules
├── presentation/
│   ├── components/      # Reusable UI components
│   ├── screens/         # Screen composables
│   └── viewmodel/       # ViewModels
├── ui/
│   └── theme/           # Theme configuration
└── util/                # Utility classes (Notification, Share)
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+ (Android 8.0 Oreo)
- Kotlin 2.0.21

### Building the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/panguangze/diary.git
   cd diary
   ```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Build and run on emulator or device:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Tests
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📖 Key Components

### ViewModels
- **HomeViewModel**: Manages home screen state and mood selection
- **HistoryViewModel**: Handles mood history loading and filtering
- **StatisticsViewModel**: Computes and presents mood statistics
- **SettingsViewModel**: Manages app configuration and preferences

### Repositories
- **AppRepository**: Main repository for app configuration and mood data
- **CheckInRepository**: Manages habit check-in data
- **HabitRepository**: Handles habit CRUD operations

### Utilities
- **NotificationHelper**: Manages local notifications and reminders
- **ShareHelper**: Handles content sharing (text and images)
- **DataBackupManager**: Implements backup/restore functionality

## 🎨 UI/UX Improvements

### Accessibility
- Semantic content descriptions for all interactive elements
- Support for screen readers
- High contrast color schemes
- Proper touch target sizes

### Animations
- Scale animations on mood button selection
- Fade-in transitions for feedback cards
- Staggered list item animations in history
- Smooth screen transitions

### Dark Mode
- Complete dark theme implementation
- System-follow option
- Manual light/dark toggle
- Optimized colors for night use

## 🔒 Security & Privacy

### Data Protection
- All data stored locally (no cloud sync by default)
- Encrypted database support ready
- Secure file sharing with FileProvider
- No analytics or tracking

### Permissions
- `POST_NOTIFICATIONS`: For reminder notifications (Android 13+)
- `INTERNET`: For future cloud sync (optional)
- File access only through SAF (Storage Access Framework)

## 📝 Documentation

### KDoc Coverage
- All public APIs documented
- Repository methods with error handling docs
- ViewModel state classes documented
- Utility classes with usage examples

### Code Quality
- Consistent error handling with try-catch
- Comprehensive logging in critical paths
- Null safety throughout
- Unit tests for ViewModels and Repositories

## 🛠️ Development

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Keep functions small and focused
- Add KDoc for public APIs

### Testing Strategy
- Unit tests for business logic
- ViewModel tests with coroutine testing
- Repository tests with test doubles
- UI tests for critical user flows (future)

### Git Workflow
- Feature branches for new work
- Pull requests for code review
- Semantic commit messages
- Linear history preferred

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **panguangze** - Initial work and maintenance

## 🙏 Acknowledgments

- Material Design 3 guidelines
- Jetpack Compose community
- Open source contributors

## 📮 Contact

For questions or support, please open an issue on GitHub.

---

Made with ❤️ for long-distance couples
