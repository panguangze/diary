plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")   // ✅ Kotlin 2.0 必须
    id("com.google.devtools.ksp")              // ✅ 版本由根 build.gradle 控制
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")    // 👈 加上
}

android {
    namespace = "com.love.diary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.love.diary"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    // ❌ 不再需要 composeOptions.kotlinCompilerExtensionVersion
    // composeOptions { ... } 整块可以删掉

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.0")

    // Compose BOM + libraries
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // ---------- 这里开始是 Hilt 相关 ----------
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-android-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    // 如果你不用 KSP，也可以改成：
    // kapt("com.google.dagger:hilt-android-compiler:2.52")
    // 并在 plugins 里加 id("kotlin-kapt")
    // ----------------------------------------

    // 图片处理
    implementation("io.coil-kt:coil-compose:2.5.0")
// 图表库 (可选，用于统计页)
    implementation("com.patrykandpatrick.vico:compose-m3:1.12.0")
// 文件处理
    implementation("com.google.code.gson:gson:2.10.1")
// 权限处理
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // Debug 工具
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}