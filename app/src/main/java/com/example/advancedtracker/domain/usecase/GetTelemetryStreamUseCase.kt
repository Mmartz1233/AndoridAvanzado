package com.example.advancedtracker.domain.usecase


import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso específico para suscribirse al stream reactivo de geolocalización.
 * Inyecta el contrato del repositorio, desacoplando la implementación concreta.
 */
class GetTelemetryStreamUseCase @Inject constructor(
    private val repository: TelemetryRepository
) {
    operator fun invoke(): Flow<TelemetryData> {
        return repository.observeTelemetry()
    }
}
