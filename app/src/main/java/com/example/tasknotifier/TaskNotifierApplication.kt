package com.example.tasknotifier

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskNotifierApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Здесь можно добавить инициализацию при необходимости
    }
}