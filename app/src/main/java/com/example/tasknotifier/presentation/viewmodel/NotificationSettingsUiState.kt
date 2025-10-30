package com.example.tasknotifier.presentation.viewmodel

import com.example.tasknotifier.data.entities.NotificationSettings
import com.example.tasknotifier.domain.model.NotificationFrequency

data class NotificationSettingsUiState(
    val settings: NotificationSettings = NotificationSettings(),
    val frequency: String = NotificationFrequency.EVERY_1_HOUR.name,
    val enabled: Boolean = true,
    val startTime: String = "09:00",
    val endTime: String = "18:00",
    val selectedDays: List<Int> = listOf(1, 2, 3, 4, 5),
    val useAdvancedSettings: Boolean = false
)