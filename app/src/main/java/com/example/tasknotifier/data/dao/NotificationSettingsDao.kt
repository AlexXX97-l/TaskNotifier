package com.example.tasknotifier.data.dao

import androidx.room.*
import com.example.tasknotifier.data.entities.NotificationSettings

@Dao
interface NotificationSettingsDao {
    @Query("SELECT * FROM notification_settings WHERE id = 1")
    suspend fun getSettings(): NotificationSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: NotificationSettings)

    @Update
    suspend fun updateSettings(settings: NotificationSettings)

    @Query("DELETE FROM notification_settings")
    suspend fun deleteAllSettings()
}