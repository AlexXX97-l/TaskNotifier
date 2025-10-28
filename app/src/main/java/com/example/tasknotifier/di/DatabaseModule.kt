package com.example.tasknotifier.di

import android.content.Context
import com.example.tasknotifier.data.database.AppDatabase
import com.example.tasknotifier.data.dao.NotificationSettingsDao
import com.example.tasknotifier.data.dao.TaskDao
import com.example.tasknotifier.data.repository.NotificationSettingsRepository
import com.example.tasknotifier.data.repository.TaskRepository
import com.example.tasknotifier.domain.usecase.NotificationUseCases
import com.example.tasknotifier.domain.usecase.TaskUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideNotificationSettingsDao(database: AppDatabase): NotificationSettingsDao {
        return database.notificationSettingsDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepository(taskDao)
    }

    @Provides
    @Singleton
    fun provideNotificationSettingsRepository(
        settingsDao: NotificationSettingsDao
    ): NotificationSettingsRepository {
        return NotificationSettingsRepository(settingsDao)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases {
        return TaskUseCases(repository)
    }

    @Provides
    @Singleton
    fun provideNotificationUseCases(
        repository: NotificationSettingsRepository
    ): NotificationUseCases {
        return NotificationUseCases(repository)
    }
}