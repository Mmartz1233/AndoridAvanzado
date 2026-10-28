package com.example.advancedtracker.domain.repository


import com.example.advancedtracker.domain.model.tracking.TelemetryData
import kotlinx.coroutines.flow.Flow

/**
 * Define el contrato de abstracción para la recuperación y envío de telemetría.
 * Implementa el principio de inversión de dependencias de SOLID.
 */
interface TelemetryRepository {

    /**
     * Provee un flujo continuo de actualizaciones de telemetría en tiempo real.
     */
    fun observeTelemetry(): Flow<TelemetryData>

    /**
     * Almacena o envía un registro específico de telemetría.
     */
    suspend fun saveTelemetry(data: TelemetryData): Result<Unit>

    /**
     * Fuerza la actualización del stream de telemetría.
     */
    suspend fun forceRefresh()
}