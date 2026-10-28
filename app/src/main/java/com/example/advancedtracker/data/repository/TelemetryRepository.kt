package com.example.advancedtracker.data.repository
import com.example.advancedtracker.domain.model.EngineStatus
import com.example.advancedtracker.domain.model.TelemetryEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class TelemetryRepository {
    private val _engines = MutableStateFlow<List<TelemetryEngine>>(initialEngines)
    val engines: Flow<List<TelemetryEngine>> = _engines.asStateFlow()

    fun getEngineById(id: String): Flow<TelemetryEngine?> {
        return _engines.map { list -> list.firstOrNull { it.id == id } }
    }

    fun updateEngineTelemetry(id: String, temperature: Double, rpm: Int, fuel: Float) {
        val currentList = _engines.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = currentList[index]
            val newStatus = when {
                temperature > 105.0 -> EngineStatus.CRITICAL_OVERHEAT
                fuel < 0.05f -> EngineStatus.MAINTENANCE
                else -> EngineStatus.ACTIVE
            }
            currentList[index] = old.copy(
                coreTemperature = temperature,
                currentRpm = rpm,
                fuelLevel = fuel,
                status = newStatus,
                failureRisk = calculateRisk(temperature, rpm)
            )
            _engines.value = currentList
        }
    }

    private fun calculateRisk(temp: Double, rpm: Int): Double {
        var baseRisk = 5.0
        if (temp > 90.0) baseRisk += 35.0
        if (temp > 105.0) baseRisk += 45.0
        if (rpm > 8000) baseRisk += 15.0
        return baseRisk.coerceAtMost(100.0)
    }

    companion object {
        private val initialEngines = listOf(
            TelemetryEngine("ENG-001", "Core Thruster Alpha", "TX-900V", EngineStatus.ACTIVE, 78.5, 0.85f, 5400, 12.4),
            TelemetryEngine("ENG-002", "Auxiliary Turbine Beta", "AT-450", EngineStatus.STANDBY, 42.1, 0.99f, 0, 1.5),
            TelemetryEngine("ENG-003", "Main Reactor Gamma", "RX-Hyperion", EngineStatus.CRITICAL_OVERHEAT, 112.4, 0.42f, 9200, 89.9),
            TelemetryEngine("ENG-004", "Hydraulic Pump Delta", "HP-77X", EngineStatus.MAINTENANCE, 55.0, 0.02f, 1200, 45.0)
        )
    }
}