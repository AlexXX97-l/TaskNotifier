package com.example.tasknotifier.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknotifier.data.entities.NotificationSettings
import com.example.tasknotifier.domain.model.DayConfiguration
import com.example.tasknotifier.domain.model.NotificationFrequency
import com.example.tasknotifier.domain.usecase.NotificationUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.tasknotifier.service.notification.NotificationReceiver
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationUseCases: NotificationUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    private val _dayConfigurations = MutableStateFlow<List<DayConfiguration>>(emptyList())
    val dayConfigurations: StateFlow<List<DayConfiguration>> = _dayConfigurations.asStateFlow()

    init {
        loadSettings()
        loadDayConfigurations()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = notificationUseCases.getSettings()
            if (settings != null) {
                // ИСПРАВЛЕНИЕ: Добавлена проверка на пустые строки при разборе daysOfWeek
                val daysList = if (settings.daysOfWeek.isNotEmpty()) {
                    settings.daysOfWeek.split(",")
                        .mapNotNull { dayString ->
                            dayString.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                        }
                } else {
                    emptyList()
                }

                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    frequency = settings.frequency,
                    enabled = settings.enabled,
                    startTime = settings.startTime ?: "09:00",
                    endTime = settings.endTime ?: "18:00",
                    selectedDays = daysList, // Используем обработанный список
                    useAdvancedSettings = settings.useAdvancedSettings
                )

                loadDayConfigurations()
            } else {
                val defaultSettings = NotificationSettings()
                notificationUseCases.saveSettings(defaultSettings)
                _uiState.value = _uiState.value.copy(
                    settings = defaultSettings,
                    frequency = defaultSettings.frequency,
                    enabled = defaultSettings.enabled,
                    startTime = defaultSettings.startTime ?: "09:00",
                    endTime = defaultSettings.endTime ?: "18:00",
                    selectedDays = defaultSettings.daysOfWeek.split(",")
                        .mapNotNull { it.trim().takeIf { str -> str.isNotEmpty() }?.toIntOrNull() },
                    useAdvancedSettings = defaultSettings.useAdvancedSettings
                )
            }
        }
    }

    private fun loadDayConfigurations() {
        viewModelScope.launch {
            val configurations = notificationUseCases.getDayConfigurations()
            _dayConfigurations.value = configurations
        }
    }

    fun updateFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun updateEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enabled = enabled)
    }

    fun updateStartTime(time: String) {
        println("ViewModel: updateStartTime called with $time")
        _uiState.value = _uiState.value.copy(startTime = time)
    }

    fun updateEndTime(time: String) {
        println("ViewModel: updateEndTime called with $time")
        _uiState.value = _uiState.value.copy(endTime = time)
    }

    fun toggleDay(day: Int) {
        val currentDays = _uiState.value.selectedDays.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(selectedDays = currentDays)
    }

    fun updateUseAdvancedSettings(useAdvanced: Boolean) {
        viewModelScope.launch {
            notificationUseCases.toggleAdvancedSettings(useAdvanced)
            _uiState.value = _uiState.value.copy(useAdvancedSettings = useAdvanced)
        }
    }

    fun updateDayConfiguration(updatedConfig: DayConfiguration) {
        viewModelScope.launch {
            val currentConfigs = _dayConfigurations.value.toMutableList()
            val index = currentConfigs.indexOfFirst { it.dayOfWeek == updatedConfig.dayOfWeek }
            if (index != -1) {
                currentConfigs[index] = updatedConfig
                _dayConfigurations.value = currentConfigs
                notificationUseCases.saveDayConfigurations(currentConfigs)
                syncWithBasicSettings(currentConfigs)
            }
        }
    }

    private fun syncWithBasicSettings(configurations: List<DayConfiguration>) {
        val enabledDays = configurations
            .filter { it.enabled }
            .map { it.dayOfWeek }
            .sorted()

        _uiState.value = _uiState.value.copy(
            selectedDays = enabledDays
        )
    }
    fun saveSettings(context: Context) {
        viewModelScope.launch {
            val state = _uiState.value
            // Убедимся, что selectedDays не содержит null или недопустимых значений
            val validDays = state.selectedDays.filter { it in 1..7 }

            val settings = state.settings.copy(
                frequency = state.frequency,
                enabled = state.enabled,
                startTime = if (state.enabled && !state.useAdvancedSettings) state.startTime else null,
                endTime = if (state.enabled && !state.useAdvancedSettings) state.endTime else null,
                daysOfWeek = if (state.enabled && !state.useAdvancedSettings) validDays.joinToString(",") else "",
                useAdvancedSettings = state.useAdvancedSettings
            )
            notificationUseCases.saveSettings(settings)

            NotificationReceiver.sendUpdateScheduleBroadcast(context)
        }
    }
}