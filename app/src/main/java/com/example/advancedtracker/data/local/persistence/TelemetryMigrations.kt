package com.example.advancedtracker.data.local.persistence


import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE telemetry_table ADD COLUMN cached_at INTEGER NOT NULL DEFAULT 0"
        )
    }
}