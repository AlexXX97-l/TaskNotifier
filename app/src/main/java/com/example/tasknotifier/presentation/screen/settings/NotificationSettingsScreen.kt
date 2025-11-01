@file:Suppress("DEPRECATION")

package com.example.tasknotifier.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.tasknotifier.domain.model.DayConfiguration
import com.example.tasknotifier.domain.model.DayOfWeek
import com.example.tasknotifier.domain.model.NotificationFrequency
import com.example.tasknotifier.presentation.component.TimePickerDialog
import com.example.tasknotifier.presentation.viewmodel.NotificationSettingsViewModel
import com.example.tasknotifier.presentation.viewmodel.NotificationSettingsUiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dayConfigurations by viewModel.dayConfigurations.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Настройки уведомлений") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Просто выходим без сохранения
                        onBack()
                    }) {
                        Icon(Icons.Outlined.Done, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Включение уведомлений
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Включить уведомления", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = viewModel::updateEnabled
                )
            }

            if (uiState.enabled) {
                // Переключатель режима настроек
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Расширенные настройки по дням", style = MaterialTheme.typography.titleMedium)
                    Switch(
                        checked = uiState.useAdvancedSettings,
                        onCheckedChange = viewModel::updateUseAdvancedSettings
                    )
                }

                if (uiState.useAdvancedSettings) {
                    // РАСШИРЕННЫЕ НАСТРОЙКИ
                    AdvancedSettingsSection(
                        dayConfigurations = dayConfigurations,
                        onConfigureClick = { navController.navigate("advancedTimeSettings") }
                    )
                } else {
                    // БАЗОВЫЕ НАСТРОЙКИ
                    BasicSettingsSection(uiState = uiState, viewModel = viewModel)
                }

                // Периодичность (общая для обоих режимов)
                FrequencySection(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AdvancedSettingsSection(
    dayConfigurations: List<DayConfiguration>,
    onConfigureClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Настройте разные временные интервалы для каждого дня недели",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onConfigureClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Настроить время для каждого дня")
        }

        CurrentConfigurationPreview(dayConfigurations = dayConfigurations)
    }
}

@Composable
private fun BasicSettingsSection(
    uiState: NotificationSettingsUiState,
    viewModel: NotificationSettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Временной интервал
        Text("Временной интервал для уведомлений", style = MaterialTheme.typography.titleSmall)
        TimeIntervalSection(
            startTime = uiState.startTime,
            endTime = uiState.endTime,
            onStartTimeChange = viewModel::updateStartTime,
            onEndTimeChange = viewModel::updateEndTime
        )

        // Дни недели
        Text("Дни недели для уведомлений", style = MaterialTheme.typography.titleSmall)
        DaysOfWeekSection(
            selectedDays = uiState.selectedDays,
            onDayToggle = viewModel::toggleDay
        )
    }
}

@Composable
private fun FrequencySection(
    uiState: NotificationSettingsUiState,
    viewModel: NotificationSettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Периодичность уведомлений", style = MaterialTheme.typography.titleSmall)
        NotificationFrequency.entries.forEach { frequency ->
            val displayName = when (frequency) {
                NotificationFrequency.EVERY_30_MIN -> "Каждые 30 минут"
                NotificationFrequency.EVERY_1_HOUR -> "Каждый час"
                NotificationFrequency.EVERY_2_HOURS -> "Каждые 2 часа"
                NotificationFrequency.EVERY_3_HOURS -> "Каждые 3 часа"
                NotificationFrequency.EVERY_6_HOURS -> "Каждые 6 часов"
                NotificationFrequency.EVERY_9_HOURS -> "Каждые 9 часов"
            }

            RadioButtonItem(
                text = displayName,
                selected = uiState.frequency == frequency.name,
                onSelect = { viewModel.updateFrequency(frequency.name) }
            )
        }
    }
}

@Composable
private fun CurrentConfigurationPreview(dayConfigurations: List<DayConfiguration>) {
    val enabledConfigs = dayConfigurations.filter { it.enabled }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Текущая конфигурация:",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (enabledConfigs.isEmpty()) {
                Text(
                    "Все дни отключены",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    enabledConfigs.forEach { config ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(config.displayName(), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${config.startTime} - ${config.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeIntervalSection(
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Уведомления будут приходить в указанный промежуток времени:",
            style = MaterialTheme.typography.bodyMedium
        )

        // Используем простые кнопки вместо TextField
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Начало", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showStartTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(startTime, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Text("до", style = MaterialTheme.typography.bodyMedium)

            Column(modifier = Modifier.weight(1f)) {
                Text("Конец", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showEndTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(endTime, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        // TimePicker диалоги
        if (showStartTimePicker) {
            TimePickerDialog(
                initialTime = parseTime(startTime),
                onTimeSelected = { time ->
                    onStartTimeChange(time)
                    showStartTimePicker = false
                },
                onDismiss = { showStartTimePicker = false }
            )
        }

        if (showEndTimePicker) {
            TimePickerDialog(
                initialTime = parseTime(endTime),
                onTimeSelected = { time ->
                    onEndTimeChange(time)
                    showEndTimePicker = false
                },
                onDismiss = { showEndTimePicker = false }
            )
        }
    }
}

@Composable
private fun DaysOfWeekSection(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Выберите дни, в которые хотите получать уведомления:",
            style = MaterialTheme.typography.bodyMedium
        )

        // Заменяем FlowRow на Column с Rows для переноса
        DaysOfWeekFlowRow(
            selectedDays = selectedDays,
            onDayToggle = onDayToggle
        )

        if (selectedDays.isNotEmpty()) {
            Text(
                "Выбраны: ${selectedDays.sorted().joinToString { getDayName(it) }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DaysOfWeekFlowRow(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit
) {
    val days = DayOfWeek.entries.chunked(4) // Разбиваем на группы по 4 дня

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { rowDays ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowDays.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day.number),
                        onClick = { onDayToggle(day.number) },
                        label = { Text(day.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Добавляем невидимые чипы для выравнивания, если в строке меньше 4 элементов
                repeat(4 - rowDays.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RadioButtonItem(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

// Вспомогательные функции
private fun parseTime(timeString: String): LocalTime {
    return try {
        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        println("Error parsing time '$timeString': ${e.message}")
        LocalTime.of(9, 0) // значение по умолчанию
    }
}

private fun getDayName(dayNumber: Int): String {
    return DayOfWeek.entries.find { it.number == dayNumber }?.displayName ?: ""
}