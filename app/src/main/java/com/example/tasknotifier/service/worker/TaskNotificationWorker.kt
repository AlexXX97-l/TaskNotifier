package com.example.tasknotifier.service.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tasknotifier.data.database.AppDatabase
import com.example.tasknotifier.data.repository.NotificationSettingsRepository
import com.example.tasknotifier.data.repository.TaskRepository
import com.example.tasknotifier.domain.model.DayConfiguration
import com.example.tasknotifier.service.notification.TaskNotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TaskNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val settingsRepository = NotificationSettingsRepository(
                database.notificationSettingsDao()
            )
            val taskRepository = TaskRepository(database.taskDao())

            val settings = settingsRepository.getSettings()
            if (settings?.enabled != true) {
                return@withContext Result.success()
            }

            val currentTime = LocalTime.now()
            val currentDay = java.time.LocalDate.now().dayOfWeek.value

            // Проверка настроек в зависимости от режима
            val shouldNotify = if (settings.useAdvancedSettings) {
                checkAdvancedSettings(settings, currentDay, currentTime)
            } else {
                checkBasicSettings(settings, currentDay, currentTime)
            }

            if (!shouldNotify) {
                return@withContext Result.success()
            }

            val activeTasks = taskRepository.getActiveTasks().first()
            if (activeTasks.isNotEmpty()) {
                TaskNotificationService.showTaskNotification(
                    context = applicationContext,
                    tasks = activeTasks
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun checkBasicSettings(
        settings: com.example.tasknotifier.data.entities.NotificationSettings,
        currentDay: Int,
        currentTime: LocalTime
    ): Boolean {
        // Проверка дней недели
        val enabledDays = settings.daysOfWeek.split(",").map { it.toInt() }
        if (!enabledDays.contains(currentDay)) {
            return false
        }

        // Проверка временного интервала
        val startTime = settings.startTime?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) }
        val endTime = settings.endTime?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) }

        if (startTime != null && endTime != null) {
            if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                return false
            }
        }

        return true
    }

    private fun checkAdvancedSettings(
        settings: com.example.tasknotifier.data.entities.NotificationSettings,
        currentDay: Int,
        currentTime: LocalTime
    ): Boolean {
        try {
            val configurations = Json.decodeFromString<List<DayConfiguration>>(settings.daysConfiguration)
            val todayConfig = configurations.find { it.dayOfWeek == currentDay && it.enabled }

            if (todayConfig == null) {
                return false
            }

            val startTime = LocalTime.parse(todayConfig.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            val endTime = LocalTime.parse(todayConfig.endTime, DateTimeFormatter.ofPattern("HH:mm"))

            return !(currentTime.isBefore(startTime) || currentTime.isAfter(endTime))
        } catch (e: Exception) {
            return false
        }
    }
}