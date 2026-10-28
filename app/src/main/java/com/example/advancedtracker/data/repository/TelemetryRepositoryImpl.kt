package com.example.advancedtracker.data.repository

import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.repository.TelemetryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación concreta del repositorio. Realiza la simulación de lecturas de sensores GPS.
 * Utiliza @Singleton para asegurar una única fuente de verdad en memoria.
 */
@Singleton
class TelemetryRepositoryImpl @Inject constructor() : TelemetryRepository {

    private val mutex = Mutex()
    private val storedTelemetryList = mutableListOf<TelemetryData>()

    // Coordenadas base (Santiago de Chile, como ejemplo geográfico de prueba)
    private var baseLatitude = -33.4489
    private var baseLongitude = -70.6693

    override fun observeTelemetry(): Flow<TelemetryData> = flow {
        var count = 0
        while (true) {
            // Simular perturbaciones leves de localización de un dispositivo en movimiento
            val randomOffsetLat = ((-100..100).random()) * 0.0001
            val randomOffsetLng = ((-100..100).random()) * 0.0001
            val instantSpeed = (20..110).random().toDouble()

            val telemetrySample = TelemetryData(
                id = UUID.randomUUID().toString(),
                latitude = baseLatitude + randomOffsetLat,
                longitude = baseLongitude + randomOffsetLng,
                speedKmh = instantSpeed,
                timestamp = Date(),
                isMocked = true
            )

            emit(telemetrySample)
            count++

            // Simular recolección continua cada 3 segundos
            delay(3000)
        }
    }

    override suspend fun saveTelemetry(data: TelemetryData): Result<Unit> {
        return mutex.withLock {
            try {
                // Simulación de retraso de almacenamiento (operación de entrada y salida asíncrona)
                delay(150)
                storedTelemetryList.add(data)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun forceRefresh() {
        // En una implementación real, dispararía una actualización de la fuente GPS.
        delay(500)
    }
}