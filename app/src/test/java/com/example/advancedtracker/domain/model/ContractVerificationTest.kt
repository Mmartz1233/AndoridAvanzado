package com.example.advancedtracker.domain.model

import org.junit.Test

class ContractVerificationTest {

    @Test
    fun verificaSmartCast() {
        val data: TelemetryData? = TelemetryData(
            deviceId = DeviceId("sensor-01"),
            timestamp = 1L,
            speedKmh = 25.0,
            temperatureCelsius = 22.0
        )

        if (verifyTelemetryPayload(data)) {
            // data es TelemetryData, sin ?. ni !!
            println("ID del dispositivo verificado: ${data.deviceId.value}")
        }
    }
}