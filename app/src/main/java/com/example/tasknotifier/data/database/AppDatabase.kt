package com.example.tasknotifier.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.tasknotifier.data.dao.TaskDao
import com.example.tasknotifier.data.dao.NotificationSettingsDao
import com.example.tasknotifier.data.entities.Task
import com.example.tasknotifier.data.entities.NotificationSettings

@Database(
    entities = [Task::class, NotificationSettings::class],
    version = 2, // ← Изменено с 1 на 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        // Миграция с версии 1 на версию 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notification_settings ADD COLUMN daysConfiguration TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE notification_settings ADD COLUMN useAdvancedSettings INTEGER NOT NULL DEFAULT 0")

                db.execSQL("""
                    UPDATE notification_settings 
                    SET daysConfiguration = '[]'
                    WHERE daysConfiguration IS NULL
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "tasknotifier.db"
                )
                    .addMigrations(MIGRATION_1_2) // ← Добавлена миграция
                    .build().also { Instance = it }
            }
        }
    }
}