package com.example.tasknotifier.di

import com.example.tasknotifier.domain.usecase.NotificationUseCases
import com.example.tasknotifier.domain.usecase.TaskUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideTaskUseCases(
        repository: com.example.tasknotifier.data.repository.TaskRepository
    ): TaskUseCases {
        return TaskUseCases(repository)
    }

    @Provides
    @Singleton
    fun provideNotificationUseCases(
        repository: com.example.tasknotifier.data.repository.NotificationSettingsRepository
    ): NotificationUseCases {
        return NotificationUseCases(repository)
    }
}