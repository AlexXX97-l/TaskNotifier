package com.example.tasknotifier.data.repository

import com.example.tasknotifier.data.dao.NotificationSettingsDao
import com.example.tasknotifier.data.entities.NotificationSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsRepository @Inject constructor(
    private val settingsDao: NotificationSettingsDao
) {
    suspend fun getSettings(): NotificationSettings? = settingsDao.getSettings()

    suspend fun insertSettings(settings: NotificationSettings) =
        settingsDao.insertSettings(settings)

    suspend fun updateSettings(settings: NotificationSettings) =
        settingsDao.updateSettings(settings)
}