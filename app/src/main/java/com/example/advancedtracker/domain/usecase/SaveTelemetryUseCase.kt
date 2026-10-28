package com.example.advancedtracker.domain.usecase


import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.repository.TelemetryRepository
import javax.inject.Inject

/**
 * Caso de uso encargado de la lógica y validaciones previas al almacenamiento de la telemetría.
 */
class SaveTelemetryUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    suspend operator fun invoke(data: TelemetryData): Result<Unit> {
        // Validación de negocio: Evitar almacenar coordenadas aberrantes o imposibles (v.g. Latitudes fuera del rango [-90, 90])
        if (data.latitude !in -90.0..90.0 || data.longitude !in -180.0..180.0) {
            return Result.failure(IllegalArgumentException("Coordenadas geográficas inválidas"))
        }
        return repository.saveTelemetry(data)
    }
}