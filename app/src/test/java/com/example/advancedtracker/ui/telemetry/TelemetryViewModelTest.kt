package com.example.advancedtracker.ui.telemetry


import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.usecase.GetTelemetryStreamUseCase
import com.example.advancedtracker.domain.usecase.SaveTelemetryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getTelemetryStreamUseCase: GetTelemetryStreamUseCase = mock()
    private val saveTelemetryUseCase: SaveTelemetryUseCase = mock()

    private lateinit var viewModel: TelemetryViewModel

    @Before
    fun setUp() {
        // Configuramos el despachador de pruebas para simular el hilo principal (Main) de Android
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando inicia tracking, el estado de UI se actualiza con coordenadas exitosas`() = runTest {
        // GIVEN
        val mockData = TelemetryData(
            id = "test-123",
            latitude = -12.0463,
            longitude = -77.0427,
            speedKmh = 55.5,
            timestamp = Date(),
            isMocked = true
        )
        whenever(getTelemetryStreamUseCase()).thenReturn(flowOf(mockData))

        // WHEN
        viewModel = TelemetryViewModel(getTelemetryStreamUseCase, saveTelemetryUseCase)
        viewModel.startTracking()

        // THEN
        val state = viewModel.uiState.value
        assertEquals(mockData, state.currentTelemetry)
        assertEquals(false, state.isLoading)
        assertEquals(null, state.lastError)
    }

    @Test
    fun `cuando falla el stream del GPS, el estado actualiza el error de manera controlada`() = runTest {
        // GIVEN (Simulamos un fallo inesperado del sensor mediante una excepción en el flow)
        val errorMessage = "Fallo de conexión de satélites GPS"
        whenever(getTelemetryStreamUseCase()).thenReturn(flow {
            throw RuntimeException(errorMessage)
        })

        // WHEN
        viewModel = TelemetryViewModel(getTelemetryStreamUseCase, saveTelemetryUseCase)
        viewModel.startTracking()

        // THEN
        val state = viewModel.uiState.value
        assertNotNull(state.lastError)
        assertTrue(state.lastError!!.contains(errorMessage))
        assertEquals(false, state.isLoading)
    }
    @Test
    fun `cuando se intenta guardar una lectura con coordenadas invalidas, se retorna error de negocio`() = runTest {
        // GIVEN: Una lectura corrupta con latitud fuera del rango estándar planetario [-90.0, 90.0]
        val corruptTelemetry = TelemetryData(
            id = "corrupt-id",
            latitude = 150.0,  // Latitud imposible
            longitude = -45.0,
            speedKmh = 0.0,
            timestamp = Date(),
            isMocked = true
        )
        val realSaveUseCase = SaveTelemetryUseCase(repository = mock()) // Usamos la regla de negocio real del caso de uso

        // WHEN
        val result = realSaveUseCase(corruptTelemetry)

        // THEN
        assertTrue(result.isFailure)
        assertEquals("Coordenadas geográficas inválidas", result.exceptionOrNull()?.message)
    }
}