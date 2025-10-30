package com.example.tasknotifier.data.database

import androidx.room.TypeConverter
import com.example.tasknotifier.domain.model.DayConfiguration
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }

    @TypeConverter
    fun fromDayConfigurationList(configurations: List<DayConfiguration>): String {
        return Json.encodeToString(configurations)
    }

    @TypeConverter
    fun toDayConfigurationList(value: String): List<DayConfiguration> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}