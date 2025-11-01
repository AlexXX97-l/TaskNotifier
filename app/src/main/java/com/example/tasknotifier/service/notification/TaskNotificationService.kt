package com.example.tasknotifier.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.tasknotifier.MainActivity
import com.example.tasknotifier.R
import com.example.tasknotifier.data.entities.Task

object TaskNotificationService {
    private const val CHANNEL_ID = "task_notifications"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Уведомления о задачах",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Показывает уведомления о текущих задачах"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showTaskNotification(context: Context, tasks: List<Task>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val highPriorityTasks = tasks.count { it.priority == "HIGH" }
        val mediumPriorityTasks = tasks.count { it.priority == "MEDIUM" }
        val lowPriorityTasks = tasks.count { it.priority == "LOW" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tn_notification)
            .setContentTitle("Текущие задачи")
            .setContentText("У вас ${tasks.size} активных задач")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Всего задач: ${tasks.size}\n" +
                                "Высокий приоритет: $highPriorityTasks\n" +
                                "Средний приоритет: $mediumPriorityTasks\n" +
                                "Низкий приоритет: $lowPriorityTasks\n" +
                                "Нажмите для просмотра всех задач"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}