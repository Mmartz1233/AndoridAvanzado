package com.example.advancedtracker.domain.model
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlinx.serialization.Serializable

@Serializable
data class TelemetryEngine(
    val id: String,
    val name: String,
    val model: String,
    val status: EngineStatus,
    val coreTemperature: Double,
    val fuelLevel: Float, // 0.0f a 1.0f
    val currentRpm: Int,
    val failureRisk: Double // Porcentaje 0.0 a 100.0
)

enum class EngineStatus {
    ACTIVE,
    STANDBY,
    CRITICAL_OVERHEAT,
    MAINTENANCE
}
class CoordinateLoggerDelegate(initialValue: Pair<Double, Double>) : ReadWriteProperty<Any?, Pair<Double, Double>> {
    private var internalCoords = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): Pair<Double, Double> {
        return internalCoords
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Pair<Double, Double>) {
        // Validación básica de coordenadas geográficas
        val (lat, lon) = value
        if (lat in -90.0..90.0 && lon in -180.0..180.0) {
            internalCoords = value
        } else {
            println("[ERROR - Telemetría] Coordenadas inválidas descartadas: Lat $lat, Lon $lon")
        }
    }
}
data class TelemetryData(
    val deviceId: DeviceId,
    val timestamp: Long,
    val speedKmh: Double,
    val temperatureCelsius: Double,
    val isAnomaly: Boolean = false
) {
    // Delegación para rastrear cambios en coordenadas de manera transparente
    var lastPosition: Pair<Double, Double> by CoordinateLoggerDelegate(0.0 to 0.0)
}

@JvmInline
value class DeviceId(val value: String) {
    init {
        require(value.isNotBlank()) { "El identificador del dispositivo no puede estar vacío." }
    }
}
@OptIn(ExperimentalContracts::class)
fun verifyTelemetryPayload(payload: TelemetryData?): Boolean {
    contract {
        returns(true) implies (payload != null)
    }
    return payload != null &&
            payload.timestamp > 0 &&
            payload.speedKmh >= 0.0 &&
            payload.temperatureCelsius in -50.0..100.0
}