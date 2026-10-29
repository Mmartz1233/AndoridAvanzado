package com.example.advancedtracker.data.local.persistence

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TelemetryEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 3)]
)
abstract class TrackingTelemetryDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
}