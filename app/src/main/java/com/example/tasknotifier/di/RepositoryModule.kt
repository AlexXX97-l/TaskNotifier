package com.example.tasknotifier.di

import com.example.tasknotifier.data.repository.NotificationSettingsRepository
import com.example.tasknotifier.data.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: com.example.tasknotifier.data.dao.TaskDao
    ): TaskRepository {
        return TaskRepository(taskDao)
    }

    @Provides
    @Singleton
    fun provideNotificationSettingsRepository(
        settingsDao: com.example.tasknotifier.data.dao.NotificationSettingsDao
    ): NotificationSettingsRepository {
        return NotificationSettingsRepository(settingsDao)
    }
}