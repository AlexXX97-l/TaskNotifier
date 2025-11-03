package com.example.tasknotifier

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.tasknotifier.data.database.AppDatabase
import com.example.tasknotifier.data.repository.NotificationSettingsRepository
import com.example.tasknotifier.presentation.screen.settings.NotificationSettingsScreen
import com.example.tasknotifier.presentation.screen.settings.AdvancedTimeSettingsScreen
import com.example.tasknotifier.presentation.screen.task.TaskEditScreen
import com.example.tasknotifier.presentation.screen.task.TaskListScreen
import com.example.tasknotifier.service.notification.TaskNotificationService
import com.example.tasknotifier.service.worker.TaskNotificationWorker
import com.example.tasknotifier.ui.theme.TaskNotifierTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestCodePostNotifications = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    requestCodePostNotifications
                )
            }
        }

        TaskNotificationService.createNotificationChannel(this)

        // Новый вызов: учитываем текущие сохранённые настройки, а не фиксированные 30 минут
        scheduleNotificationWorkerDynamic()

        setContent {
            TaskNotifierTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "taskList"
                ) {
                    composable("taskList") {
                        TaskListScreen(
                            onAddTask = { navController.navigate("editTask") },
                            onEditTask = { taskId ->
                                navController.navigate("editTask/$taskId")
                            },
                            onOpenSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }

                    composable("editTask") {
                        TaskEditScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("editTask/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                        TaskEditScreen(
                            taskId = taskId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        NotificationSettingsScreen(
                            navController = navController,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("advancedTimeSettings") {
                        AdvancedTimeSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    /**
     * Новый вариант: читает настройки из БД и подбирает интервал.
     * Если настроек нет — ничего не запускает (или можно поставить дефолт, например 60 минут).
     */
    private fun scheduleNotificationWorkerDynamic() {
        lifecycleScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(applicationContext)
            val settingsRepository = NotificationSettingsRepository(database.notificationSettingsDao())
            val settings = settingsRepository.getSettings()

            if (settings?.enabled == true) {
                val intervalMinutes = when (settings.frequency) {
                    "EVERY_30_MIN" -> 30L
                    "EVERY_1_HOUR" -> 60L
                    "EVERY_2_HOURS" -> 120L
                    "EVERY_3_HOURS" -> 180L
                    "EVERY_6_HOURS" -> 360L
                    "EVERY_9_HOURS" -> 540L
                    else -> 60L // дефолтный вариант
                }

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(true)
                    .build()

                // Отменяем любую существующую работу перед постановкой
                WorkManager.getInstance(applicationContext)
                    .cancelUniqueWork("task_notification_worker")

                val notificationWork = PeriodicWorkRequestBuilder<TaskNotificationWorker>(
                    intervalMinutes, TimeUnit.MINUTES
                )
                    .setConstraints(constraints)
                    .addTag("task_notification")
                    .build()

                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    "task_notification_worker",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    notificationWork
                )
            } else {
                // Если уведомления отключены — убедимся, что работы нет
                WorkManager.getInstance(applicationContext)
                    .cancelUniqueWork("task_notification_worker")
            }

            // Дополнительно можно отправить broadcast для синхронизации (не обязательно):
            // NotificationReceiver.sendUpdateScheduleBroadcast(applicationContext)
        }
    }

    // Старый метод scheduleNotificationWorker() удалён,
    // чтобы не путать с динамическим.
}