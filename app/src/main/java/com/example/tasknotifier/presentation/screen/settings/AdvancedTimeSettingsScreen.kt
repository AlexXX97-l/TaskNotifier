package com.example.tasknotifier.presentation.screen.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.tasknotifier.domain.model.DayConfiguration
import com.example.tasknotifier.presentation.component.TimePickerDialog
import com.example.tasknotifier.presentation.viewmodel.NotificationSettingsViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimeSettingsScreen(
    onBack: () -> Unit,
    context: Context,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val dayConfigurations by viewModel.dayConfigurations.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Расширенные настройки времени") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveSettings(context)
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Настройте временные интервалы для каждого дня недели:",
                style = MaterialTheme.typography.bodyLarge
            )

            dayConfigurations.forEach { config ->
                DayConfigurationItem(
                    configuration = config,
                    onConfigurationUpdate = viewModel::updateDayConfiguration
                )
                HorizontalDivider()
            }

            Text(
                "Пример: в понедельник и вторник - с 13:00 до 22:00, в среду-пятницу - с 07:00 до 16:00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayConfigurationItem(
    configuration: DayConfiguration,
    onConfigurationUpdate: (DayConfiguration) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (configuration.enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок дня
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    configuration.displayName(),
                    style = MaterialTheme.typography.titleMedium
                )

                Switch(
                    checked = configuration.enabled,
                    onCheckedChange = { enabled ->
                        onConfigurationUpdate(configuration.copy(enabled = enabled))
                    }
                )
            }

            if (configuration.enabled) {
                Spacer(modifier = Modifier.height(12.dp))

                // Выбор времени
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Начало времени
                    Column(modifier = Modifier.weight(1f)) {
                        Text("С", style = MaterialTheme.typography.labelMedium)
                        Button(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(configuration.startTime, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Text("до", style = MaterialTheme.typography.bodyMedium)

                    // Конец времени
                    Column(modifier = Modifier.weight(1f)) {
                        Text("До", style = MaterialTheme.typography.labelMedium)
                        Button(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(configuration.endTime, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // TimePicker диалоги
                if (showStartTimePicker) {
                    TimePickerDialog(
                        initialTime = parseTime(configuration.startTime),
                        onTimeSelected = { time ->
                            onConfigurationUpdate(configuration.copy(startTime = time))
                            showStartTimePicker = false
                        },
                        onDismiss = { showStartTimePicker = false }
                    )
                }

                if (showEndTimePicker) {
                    TimePickerDialog(
                        initialTime = parseTime(configuration.endTime),
                        onTimeSelected = { time ->
                            onConfigurationUpdate(configuration.copy(endTime = time))
                            showEndTimePicker = false
                        },
                        onDismiss = { showEndTimePicker = false }
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Уведомления отключены для этого дня",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Вспомогательная функция для парсинга времени
private fun parseTime(timeString: String): LocalTime {
    return try {
        LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        LocalTime.of(9, 0)
    }
}