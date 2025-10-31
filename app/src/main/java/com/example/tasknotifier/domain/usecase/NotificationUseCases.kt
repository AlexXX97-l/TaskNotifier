package com.example.tasknotifier.domain.usecase

import com.example.tasknotifier.data.entities.NotificationSettings
import com.example.tasknotifier.data.repository.NotificationSettingsRepository
import com.example.tasknotifier.domain.model.DayConfiguration
import com.example.tasknotifier.domain.model.defaultDayConfigurations
import kotlinx.serialization.json.Json
import javax.inject.Inject

class NotificationUseCases @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    suspend fun getSettings(): NotificationSettings? = repository.getSettings()

    suspend fun saveSettings(settings: NotificationSettings) =
        repository.insertSettings(settings)

    suspend fun getDayConfigurations(): List<DayConfiguration> {
        val settings = repository.getSettings()
        return if (settings?.daysConfiguration?.isNotEmpty() == true && settings.daysConfiguration != "[]") {
            try {
                Json.decodeFromString(settings.daysConfiguration)
            } catch (e: Exception) {
                defaultDayConfigurations
            }
        } else {
            defaultDayConfigurations
        }
    }

    suspend fun saveDayConfigurations(configurations: List<DayConfiguration>) {
        val currentSettings = repository.getSettings() ?: NotificationSettings()
        val updatedSettings = currentSettings.copy(
            daysConfiguration = Json.encodeToString(configurations),
            useAdvancedSettings = true
        )
        repository.insertSettings(updatedSettings)
    }

    suspend fun toggleAdvancedSettings(useAdvanced: Boolean) {
        val currentSettings = repository.getSettings() ?: NotificationSettings()
        val updatedSettings = currentSettings.copy(useAdvancedSettings = useAdvanced)
        repository.insertSettings(updatedSettings)
    }
}