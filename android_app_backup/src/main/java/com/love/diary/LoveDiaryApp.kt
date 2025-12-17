package com.love.diary

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用主类，用于初始化Hilt依赖注入
 * 在AndroidManifest.xml中引用
 */
@HiltAndroidApp
class LoveDiaryApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 可以在这里进行应用初始化
    }
}