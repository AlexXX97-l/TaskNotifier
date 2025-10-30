package com.example.tasknotifier.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime

@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(initialTime.minute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите время") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Часы
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

                    // Минуты
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(timeString)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

//@Composable
//private fun TimeNumberPicker(
//    value: Int,
//    onValueChange: (Int) -> Unit,
//    range: IntRange
//) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        IconButton(
//            onClick = {
//                val newValue = if (value >= range.last) range.first else value + 1
//                onValueChange(newValue)
//            },
//            modifier = Modifier.size(48.dp)
//        ) {
//            Text("▲", style = MaterialTheme.typography.titleMedium)
//        }
//
//        Text(
//            text = String.format("%02d", value),
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        IconButton(
//            onClick = {
//                val newValue = if (value <= range.first) range.last else value - 1
//                onValueChange(newValue)
//            },
//            modifier = Modifier.size(48.dp)
//        ) {
//            Text("▼", style = MaterialTheme.typography.titleMedium)
//        }
//    }
//}