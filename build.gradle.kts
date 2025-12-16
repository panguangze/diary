plugins {
    // Kotlin Gradle plugin（统一版本）
    kotlin("android") version "2.0.21" apply false
    kotlin("plugin.compose") version "2.0.21" apply false
    // KSP 插件
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    // Android Gradle Plugin - using 8.3.2 for compatibility with the build environment
    // Version 8.7.0 was not available in the current Gradle Plugin Repository
    id("com.android.application") version "8.3.2" apply false

    id("com.google.dagger.hilt.android") version "2.52" apply false
}
