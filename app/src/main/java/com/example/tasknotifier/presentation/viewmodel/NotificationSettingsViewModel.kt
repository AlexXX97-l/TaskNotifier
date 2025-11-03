package com.example.tasknotifier.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknotifier.data.entities.NotificationSettings
import com.example.tasknotifier.domain.model.DayConfiguration
import com.example.tasknotifier.domain.usecase.NotificationUseCases
import com.example.tasknotifier.service.notification.NotificationReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationUseCases: NotificationUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    private val _dayConfigurations = MutableStateFlow<List<DayConfiguration>>(emptyList())
    val dayConfigurations: StateFlow<List<DayConfiguration>> = _dayConfigurations.asStateFlow()

    // UI события (одноразовые)
    sealed interface UiEvent {
        data object SettingsSaved : UiEvent
    }
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Флаг для отслеживания изменений в расширенных настройках
    private var hasUnsavedAdvancedChanges = false

    init {
        loadSettings()
        loadDayConfigurations()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = notificationUseCases.getSettings()
            if (settings != null) {
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
                    selectedDays = daysList,
                    useAdvancedSettings = settings.useAdvancedSettings
                )

                loadDayConfigurations()
            } else {
                _uiState.value = _uiState.value.copy(
                    settings = NotificationSettings()
                )
            }
        }
    }

    private fun loadDayConfigurations() {
        viewModelScope.launch {
            val configs = notificationUseCases.getDayConfigurations()
            _dayConfigurations.value = configs
        }
    }

    fun updateFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun updateEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enabled = enabled)
    }

    fun updateStartTime(time: String) {
        _uiState.value = _uiState.value.copy(startTime = time)
    }

    fun updateEndTime(time: String) {
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
            if (useAdvanced && _dayConfigurations.value.isEmpty()) {
                loadDayConfigurations()
            }
        }
    }

    fun updateDayConfiguration(updatedConfig: DayConfiguration) {
        viewModelScope.launch {
            val currentConfigs = _dayConfigurations.value.toMutableList()
            val index = currentConfigs.indexOfFirst { it.dayOfWeek == updatedConfig.dayOfWeek }
            if (index != -1) {
                currentConfigs[index] = updatedConfig
                _dayConfigurations.value = currentConfigs
                hasUnsavedAdvancedChanges = true

                notificationUseCases.saveDayConfigurations(currentConfigs)

                val refreshed = notificationUseCases.getSettings()
                if (refreshed != null) {
                    _uiState.value = _uiState.value.copy(settings = refreshed)
                }

                syncWithBasicSettings(currentConfigs)
                hasUnsavedAdvancedChanges = false
            }
        }
    }

    private fun syncWithBasicSettings(configurations: List<DayConfiguration>) {
        val enabledDays = configurations
            .filter { it.enabled }
            .map { it.dayOfWeek }
            .sorted()
        _uiState.value = _uiState.value.copy(selectedDays = enabledDays)
    }

    fun saveSettings(context: Context) {
        viewModelScope.launch {
            val state = _uiState.value
            val validDays = state.selectedDays.filter { it in 1..7 }

            val freshFromDb = notificationUseCases.getSettings()
            val daysConfigurationJson = if (state.useAdvancedSettings) {
                freshFromDb?.daysConfiguration ?: state.settings.daysConfiguration
            } else {
                ""
            }

            val base = freshFromDb ?: state.settings

            val settingsToSave = base.copy(
                frequency = state.frequency,
                enabled = state.enabled,
                startTime = if (state.enabled && !state.useAdvancedSettings) state.startTime else null,
                endTime = if (state.enabled && !state.useAdvancedSettings) state.endTime else null,
                daysOfWeek = if (state.enabled && !state.useAdvancedSettings) validDays.joinToString(",") else "",
                daysConfiguration = daysConfigurationJson,
                useAdvancedSettings = state.useAdvancedSettings
            )

            notificationUseCases.saveSettings(settingsToSave)
            _uiState.value = _uiState.value.copy(settings = settingsToSave)
            hasUnsavedAdvancedChanges = false

            NotificationReceiver.sendUpdateScheduleBroadcast(context)

            // Отправляем событие для UI
            _events.trySend(UiEvent.SettingsSaved)
        }
    }
}