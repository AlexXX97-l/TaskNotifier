package com.example.tasknotifier.service.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tasknotifier.data.database.AppDatabase
import com.example.tasknotifier.data.repository.NotificationSettingsRepository
import com.example.tasknotifier.service.worker.TaskNotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" -> {
                // Перезапуск уведомлений после перезагрузки устройства
                CoroutineScope(Dispatchers.IO).launch {
                    restartNotificationsAfterBoot(context)
                }
            }
            "UPDATE_NOTIFICATION_SCHEDULE" -> {
                // Обновление расписания уведомлений при изменении настроек
                CoroutineScope(Dispatchers.IO).launch {
                    updateNotificationSchedule(context)
                }
            }
        }
    }

    private suspend fun restartNotificationsAfterBoot(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val settingsRepository = NotificationSettingsRepository(
            database.notificationSettingsDao()
        )

        val settings = settingsRepository.getSettings()
        if (settings?.enabled == true) {
            scheduleNotificationWorker(context, settings.frequency)
        }
    }

    private suspend fun updateNotificationSchedule(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val settingsRepository = NotificationSettingsRepository(
            database.notificationSettingsDao()
        )

        val settings = settingsRepository.getSettings()
        if (settings?.enabled == true) {
            scheduleNotificationWorker(context, settings.frequency)
        } else {
            cancelNotificationWorker(context)
        }
    }

    private fun scheduleNotificationWorker(context: Context, frequency: String) {
        val workManager = WorkManager.getInstance(context)

        // Отменяем существующую работу
        workManager.cancelUniqueWork("task_notification_worker")

        val intervalMinutes = when (frequency) {
            "EVERY_30_MIN" -> 30L
            "EVERY_1_HOUR" -> 60L
            "EVERY_2_HOURS" -> 120L
            "EVERY_3_HOURS" -> 180L
            "EVERY_6_HOURS" -> 360L
            "EVERY_9_HOURS" -> 540L
            else -> 60L // По умолчанию каждый час
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .setRequiresBatteryNotLow(true)
            .build()

        val notificationWork = PeriodicWorkRequestBuilder<TaskNotificationWorker>(
            intervalMinutes, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .addTag("task_notification")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "task_notification_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            notificationWork
        )
    }

    private fun cancelNotificationWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("task_notification_worker")
    }

    companion object {
        fun sendUpdateScheduleBroadcast(context: Context) {
            val intent = Intent("UPDATE_NOTIFICATION_SCHEDULE")
            context.sendBroadcast(intent)
        }
    }
}