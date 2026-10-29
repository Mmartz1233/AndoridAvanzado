package com.example.advancedtracker.data.local.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "telemetry_table",
    indices = [Index(value = ["timestamp"])]
)
data class TelemetryEntity(
    @PrimaryKey
    val id: String,

    val latitude: Double,

    val longitude: Double,

    @ColumnInfo(name = "speed_kmh")
    val speedKmh: Double,

    val timestamp: Long,
    @ColumnInfo(name = "is_mocked")

    val isMocked: Boolean,
    @ColumnInfo(name = "cached_at", defaultValue = "0")

    val cachedAt: Long = 0L,

    @ColumnInfo(name = "cache_source", defaultValue = "'remote'")
    val cacheSource: String = "remote"
)