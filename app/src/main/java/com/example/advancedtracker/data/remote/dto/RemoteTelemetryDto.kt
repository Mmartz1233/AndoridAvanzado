package com.example.advancedtracker.data.remote.dto

import com.example.advancedtracker.domain.model.tracking.TelemetryData
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class RemoteTelemetryDto(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val timestamp: Long,
    val isMocked: Boolean
)

fun RemoteTelemetryDto.toTelemetryData(): TelemetryData = TelemetryData(
    id = id,
    latitude = latitude,
    longitude = longitude,
    speedKmh = speedKmh,
    timestamp = Date(timestamp),
    isMocked = isMocked
)

fun TelemetryData.toRemoteDto(): RemoteTelemetryDto = RemoteTelemetryDto(
    id = id,
    latitude = latitude,
    longitude = longitude,
    speedKmh = speedKmh,
    timestamp = timestamp.time,
    isMocked = isMocked
)