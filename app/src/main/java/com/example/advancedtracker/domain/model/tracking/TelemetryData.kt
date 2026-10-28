package com.example.advancedtracker.domain.model.tracking

import java.util.Date

/**
 * Representa la entidad de datos de telemetría inmutable pura del negocio.
 * Es completamente independiente de bases de datos locales o respuestas HTTP.
 */
data class TelemetryData(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val timestamp: Date,
    val isMocked: Boolean
)