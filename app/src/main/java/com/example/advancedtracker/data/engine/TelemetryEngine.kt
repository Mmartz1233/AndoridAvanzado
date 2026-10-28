package com.example.advancedtracker.data.engine

import com.example.advancedtracker.domain.model.DeviceId
import com.example.advancedtracker.domain.model.TelemetryData
import com.example.advancedtracker.domain.model.verifyTelemetryPayload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.random.Random

class TelemetryEngine(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val random = Random(System.currentTimeMillis())

    /**
     * Flujo frío que simula la generación de datos GPS y de sensores.
     * Cambia de contexto dinámicamente para proteger el hilo principal.
     */
    fun startTelemetryEmission(deviceId: DeviceId, intervalMs: Long): Flow<TelemetryData> = flow {
        var seqLat = 4.7110 // Coordenadas base (Bogotá, CO)
        var seqLon = -74.0721

        while (true) {
            // Operaciones con uso intensivo de CPU se asocian al despachador de cálculo
            val telemetry = withContext(defaultDispatcher) {
                val rawSpeed = random.nextDouble(0.0, 120.0)
                val rawTemp = random.nextDouble(15.0, 45.0)

                // Desviación geográfica incremental simulada
                seqLat += random.nextDouble(-0.001, 0.001)
                seqLon += random.nextDouble(-0.001, 0.001)

                val isAnomalyDetected = rawTemp > 40.0 || rawSpeed > 110.0

                TelemetryData(
                    deviceId = deviceId,
                    timestamp = System.currentTimeMillis(),
                    speedKmh = rawSpeed,
                    temperatureCelsius = rawTemp,
                    isAnomaly = isAnomalyDetected
                ).apply {
                    lastPosition = Pair(seqLat, seqLon)
                }
            }

            // Validamos la integridad estructural utilizando el contrato desarrollado en el Paso 2
            if (verifyTelemetryPayload(telemetry)) {
                // Simulación de guardado interno persistente rápido usando el contexto de I/O
                withContext(ioDispatcher) {
                    saveTelemetryLogToDisk(telemetry)
                }

                emit(telemetry)
            } else {
                println("[ALERTA] Estructura de telemetría corrupta. Descartando emisión.")
            }

            delay(intervalMs)
        }
    }.flowOn(defaultDispatcher)

    private fun saveTelemetryLogToDisk(data: TelemetryData) {
        // Simulación rápida de escritura física o logging en almacenamiento de alta velocidad.
        // En este laboratorio emulamos el consumo del hilo I/O imprimiendo en consola de bajo nivel.
        val threadName = Thread.currentThread().name
        println("[LOG DISCO] [$threadName] Registrando telemetría para: ${data.deviceId.value} a velocidad ${"%.2f".format(data.speedKmh)} km/h")
    }
}