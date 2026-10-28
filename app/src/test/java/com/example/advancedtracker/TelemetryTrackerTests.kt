package com.example.advancedtracker



import app.cash.turbine.test
import com.example.advancedtracker.data.engine.TelemetryEngine
import com.example.advancedtracker.domain.model.DeviceId
import com.example.advancedtracker.presentation.TelemetryUiState
import com.example.advancedtracker.presentation.TelemetryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryTrackerTests {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var telemetryEngine: TelemetryEngine
    private lateinit var viewModel: TelemetryViewModel

    @Before
    fun setUp() {
        // Redireccionamos el despachador principal de Android al entorno virtual de pruebas
        Dispatchers.setMain(testDispatcher)
        telemetryEngine = TelemetryEngine(defaultDispatcher = testDispatcher, ioDispatcher = testDispatcher)
        viewModel = TelemetryViewModel(telemetryEngine, dispatcher = testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando se inicia el monitoreo, cambia el estado a cargando y luego emite telemetria activa`() = runTest(testDispatcher) {
        val targetId = DeviceId("PRO_TRACK_001")

        // Utilizamos Turbine para escuchar los cambios secuenciales de estado en StateFlow
        viewModel.uiState.test {
            // El estado inicial debe ser Idle
            assertEquals(TelemetryUiState.Idle, awaitItem())

            // Iniciamos simulación con intervalo rápido de 1000ms
            viewModel.startTracking(targetId, 1000L)

            // Siguiente cambio inmediato: Loading
            assertEquals(TelemetryUiState.Loading, awaitItem())

            // Simulamos el paso del tiempo virtual para detonar la primera emisión
            advanceTimeBy(1005)

            // Siguiente emisión esperada: Active
            val activeState = awaitItem()
            assertTrue(activeState is TelemetryUiState.Active)

            val payload = (activeState as TelemetryUiState.Active).data
            assertEquals("PRO_TRACK_001", payload.deviceId.value)

            // Detenemos de forma limpia la simulación
            viewModel.stopTracking()
            assertEquals(TelemetryUiState.Idle, awaitItem())
        }
    }

    @Test
    fun `cuando ocurre una lectura anomalas, se emite una alerta a traves del flujo caliente SharedFlow`() = runTest(testDispatcher) {
        val targetId = DeviceId("ANOMALY_MONITOR")

        viewModel.alertEvents.test {
            viewModel.startTracking(targetId, 100L)

            // Ejecutamos varios ciclos rápidos de simulación para forzar probabilísticamente un evento extremo
            advanceTimeBy(1500)

            // Esperamos al menos una emisión de alerta registrada en el búfer caliente
            val alertMessage = awaitItem()
            assertTrue(alertMessage.contains("ANOMALY_MONITOR") || alertMessage.contains("¡ANOMALÍA DETECTADA!"))

            viewModel.stopTracking()
        }
    }
}