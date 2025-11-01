package com.example.tasknotifier

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.tasknotifier.presentation.screen.settings.NotificationSettingsScreen
import com.example.tasknotifier.presentation.screen.settings.AdvancedTimeSettingsScreen
import com.example.tasknotifier.presentation.screen.task.TaskEditScreen
import com.example.tasknotifier.presentation.screen.task.TaskListScreen
import com.example.tasknotifier.service.notification.TaskNotificationService
import com.example.tasknotifier.service.worker.TaskNotificationWorker
import com.example.tasknotifier.ui.theme.TaskNotifierTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestCodePostNotifications = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestCodePostNotifications)
            }
        }
        TaskNotificationService.createNotificationChannel(this)
        scheduleNotificationWorker()

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
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }

    private fun scheduleNotificationWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(true)
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<TaskNotificationWorker>(
            30, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "task_notification_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            notificationWork
        )
    }
}