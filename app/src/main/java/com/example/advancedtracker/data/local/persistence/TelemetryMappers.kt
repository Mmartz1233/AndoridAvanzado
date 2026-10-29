package com.example.advancedtracker.data.local.persistence


import com.example.advancedtracker.data.remote.dto.RemoteTelemetryDto
import com.example.advancedtracker.domain.persistence.TelemetryRecord
import java.util.Date

fun RemoteTelemetryDto.toStoredEntity(): TelemetryEntity = TelemetryEntity(
    id = id,
    latitude = latitude,
    longitude = longitude,
    speedKmh = speedKmh,
    timestamp = timestamp,
    isMocked = isMocked,
    cachedAt = System.currentTimeMillis()
)

fun TelemetryEntity.toRecord(): TelemetryRecord = TelemetryRecord(
    id = id,
    latitude = latitude,
    longitude = longitude,
    speedKmh = speedKmh,
    timestamp = Date(timestamp),
    isMocked = isMocked
)