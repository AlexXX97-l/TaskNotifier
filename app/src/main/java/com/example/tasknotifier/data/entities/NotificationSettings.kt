package com.example.tasknotifier.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.tasknotifier.data.database.Converters

@Entity(tableName = "notification_settings")
@TypeConverters(Converters::class)
data class NotificationSettings(
    @PrimaryKey
    val id: Int = 1,
    val frequency: String = "EVERY_1_HOUR",
    val enabled: Boolean = true,
    val daysConfiguration: String = "[]", // JSON строка с List<DayConfiguration>
    val useAdvancedSettings: Boolean = false, // Флаг использования расширенных настроек
    val startTime: String? = "09:00",
    val endTime: String? = "18:00",
    val daysOfWeek: String = "1,2,3,4,5"
)