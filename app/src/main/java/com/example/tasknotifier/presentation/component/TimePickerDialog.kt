package com.example.tasknotifier.presentation.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime

@SuppressLint("UnrememberedMutableState")
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Выберите время", style = MaterialTheme.typography.titleMedium)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Часы", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("−", style = MaterialTheme.typography.titleLarge)
                            }

                            Text(
                                text = String.format("%02d", selectedHour),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            IconButton(
                                onClick = {
                                    selectedHour = if (selectedHour == 23) 0 else selectedHour + 1
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Минуты", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("−", style = MaterialTheme.typography.titleLarge)
                            }

                            Text(
                                text = String.format("%02d", selectedMinute),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            IconButton(
                                onClick = {
                                    selectedMinute = if (selectedMinute == 59) 0 else selectedMinute + 1
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }

                Text(
                    "Выбрано: ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }

                    Button(
                        onClick = {
                            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                            onTimeSelected(timeString)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}