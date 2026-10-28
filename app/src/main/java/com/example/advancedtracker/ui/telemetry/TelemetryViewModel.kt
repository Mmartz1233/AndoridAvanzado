package com.example.advancedtracker.ui.telemetry


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.usecase.GetTelemetryStreamUseCase
import com.example.advancedtracker.domain.usecase.SaveTelemetryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Gestiona el estado de la UI y los flujos de comunicación con el negocio de telemetría.
 * Implementa el patrón de Flujo de Datos Unidireccional (UDF).
 */
@HiltViewModel
class TelemetryViewModel @Inject constructor(
    private val getTelemetryStreamUseCase: GetTelemetryStreamUseCase,
    private val saveTelemetryUseCase: SaveTelemetryUseCase
) : ViewModel() {

    // Estado interno mutable encapsulado (UDF)
    private val _uiState = MutableStateFlow(TelemetryUiState())
    // Estado expuesto inmutable para observación segura en Compose
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    // Flujo para la propagación segura de eventos que se consumen una sola vez (como Toasts o navegaciones)
    private val _uiEvent = MutableSharedFlow<TelemetryUiEvent>()
    val uiEvent: SharedFlow<TelemetryUiEvent> = _uiEvent.asSharedFlow()

    private var trackingJob: Job? = null

    init {
        // Arranca por defecto observando los cambios
        startTracking()
    }

    /**
     * Inicia de forma asíncrona la recolección del flujo reactivo del caso de uso.
     */
    fun startTracking() {
        if (trackingJob?.isActive == true) return

        _uiState.update { it.copy(isLoading = true, isTrackingActive = true, lastError = null) }

        trackingJob = viewModelScope.launch {
            getTelemetryStreamUseCase()
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastError = exception.localizedMessage ?: "Error desconocido de telemetría"
                        )
                    }
                    _uiEvent.emit(TelemetryUiEvent.ShowToastMessage("Fallo crítico del stream de geolocalización"))
                }
                .collect { telemetryData ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentTelemetry = telemetryData
                        )
                    }
                }
        }
    }

    /**
     * Cancela la recolección activa del flujo y actualiza el estado de la UI.
     */
    fun stopTracking() {
        trackingJob?.cancel()
        _uiState.update { it.copy(isTrackingActive = false, isLoading = false) }
        viewModelScope.launch {
            _uiEvent.emit(TelemetryUiEvent.ShowToastMessage("Seguimiento pausado por el usuario"))
        }
    }

    /**
     * Guarda la lectura actual de telemetría haciendo uso del caso de uso correspondiente.
     */
    fun persistCurrentTelemetry() {
        val currentData = _uiState.value.currentTelemetry ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = saveTelemetryUseCase(currentData)
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    _uiEvent.emit(TelemetryUiEvent.ShowToastMessage("Lectura grabada localmente de manera segura"))
                },
                onFailure = { error ->
                    _uiEvent.emit(TelemetryUiEvent.ShowToastMessage("Error al guardar: ${error.message}"))
                }
            )
        }
    }

    /**
     * Emite un evento asíncrono para navegar al panel de detalle.
     */
    fun navigateToTelemetryDetail() {
        val currentId = _uiState.value.currentTelemetry?.id ?: return
        viewModelScope.launch {
            _uiEvent.emit(TelemetryUiEvent.NavigateToDetails(currentId))
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel() // Se asegura de cancelar las corrutinas activas al destruirse el ViewModel
    }
}