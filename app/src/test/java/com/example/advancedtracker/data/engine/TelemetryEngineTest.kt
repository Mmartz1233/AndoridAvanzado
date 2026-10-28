package com.example.advancedtracker.data.engine

import com.example.advancedtracker.domain.model.DeviceId
import com.example.advancedtracker.domain.model.TelemetryData
import com.example.advancedtracker.domain.model.verifyTelemetryPayload

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryEngineTest {

    @Test
    fun emiteDuranteTresSegundosYSeCancela() = runBlocking {
        val engine = TelemetryEngine()
        val readings = mutableListOf<TelemetryData>()

        val collector = launch {
            engine.startTelemetryEmission(
                deviceId = DeviceId("sensor-01"),
                intervalMs = 200L
            ).collect { data ->
                readings.add(data)
                println("Recibido: ${data.deviceId.value}, ${data.speedKmh} km/h")
            }
        }

        delay(3_000L)
        collector.cancelAndJoin()

        assertTrue("No se recibió telemetría", readings.isNotEmpty())
        assertTrue("Hay lecturas inválidas", readings.all(::verifyTelemetryPayload))
        assertTrue("La recolección sigue activa", collector.isCancelled)
    }


    @Test
    fun rechazaTelemetriaConValoresInvalidos() {
        val lecturaValida = TelemetryData(
            deviceId = DeviceId("DEVICE-01"),
            timestamp = 1L,
            speedKmh = 50.0,
            temperatureCelsius = 25.0
        )

        val timestampInvalido = lecturaValida.copy(timestamp = -1L)
        val velocidadInvalida = lecturaValida.copy(speedKmh = -5.0)
        val temperaturaInvalida = lecturaValida.copy(temperatureCelsius = 120.0)

        assertTrue(verifyTelemetryPayload(lecturaValida))
        assertFalse(verifyTelemetryPayload(timestampInvalido))
        assertFalse(verifyTelemetryPayload(velocidadInvalida))
        assertFalse(verifyTelemetryPayload(temperaturaInvalida))
    }

}