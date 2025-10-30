package com.example.tasknotifier.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DayConfiguration(
    val dayOfWeek: Int, // 1-7 (Monday-Sunday)
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val enabled: Boolean = true
) {
    fun displayName(): String {
        return DayOfWeek.entries.find { it.number == dayOfWeek }?.displayName ?: "День $dayOfWeek"
    }
}

// Дефолтные конфигурации
val defaultDayConfigurations = listOf(
    DayConfiguration(1, "13:00", "22:00"), // Понедельник
    DayConfiguration(2, "13:00", "22:00"), // Вторник
    DayConfiguration(3, "07:00", "16:00"), // Среда
    DayConfiguration(4, "07:00", "16:00"), // Четверг
    DayConfiguration(5, "07:00", "16:00"), // Пятница
    DayConfiguration(6, "10:00", "16:00"), // Суббота
    DayConfiguration(7, "10:00", "16:00")  // Воскресенье
)