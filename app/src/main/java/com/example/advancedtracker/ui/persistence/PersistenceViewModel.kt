package com.example.advancedtracker.ui.persistence


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.advancedtracker.domain.persistence.ObserveStoredTelemetryUseCase
import com.example.advancedtracker.domain.persistence.ObserveTelemetrySettingsUseCase
import com.example.advancedtracker.domain.persistence.RefreshResult
import com.example.advancedtracker.domain.persistence.RefreshStoredTelemetryUseCase
import com.example.advancedtracker.domain.persistence.SaveTelemetrySettingsUseCase
import com.example.advancedtracker.domain.persistence.TelemetryRecord
import com.example.advancedtracker.domain.persistence.TelemetrySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersistenceUiState(
    val records: List<TelemetryRecord> = emptyList(),
    val reading: Boolean = true,
    val refreshing: Boolean = false,
    val message: String = "",
    val storageError: String? = null,
    val settings: TelemetrySettings = TelemetrySettings(),
    val settingsReady: Boolean = false,
    val settingsError: String? = null
)

@HiltViewModel
class PersistenceViewModel @Inject constructor(
    private val observeRecords: ObserveStoredTelemetryUseCase,
    private val observeSettings: ObserveTelemetrySettingsUseCase,
    private val refreshRecords: RefreshStoredTelemetryUseCase,
    private val saveSettings: SaveTelemetrySettingsUseCase
) : ViewModel() {
    private val mutableState = MutableStateFlow(PersistenceUiState())
    val state = mutableState.asStateFlow()
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            observeRecords()
                .catch { error ->
                    if (error is CancellationException) throw error
                    mutableState.update {
                        it.copy(reading = false, storageError = "No fue posible leer Room o aplicar las preferencias.")
                    }
                }
                .collect { records ->
                    mutableState.update {
                        it.copy(records = records, reading = false, storageError = null)
                    }
                }
        }
        viewModelScope.launch {
            observeSettings()
                .catch { error ->
                    if (error is CancellationException) throw error
                    mutableState.update {
                        it.copy(settingsReady = false, settingsError = "No fue posible leer DataStore.")
                    }
                }
                .collect { settings ->
                    mutableState.update {
                        it.copy(settings = settings, settingsReady = true, settingsError = null)
                    }
                }
        }
        viewModelScope.launch {
            try {
                observeSettings().first()
                refresh()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(message = "La actualización requiere preferencias accesibles.") }
            }
        }
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            mutableState.update { it.copy(refreshing = true, message = "Actualizando desde la API…") }
            try {
                val message = when (val result = refreshRecords()) {
                    is RefreshResult.Updated -> "Guardados ${result.receivedCount} registros recibidos de la API."
                    is RefreshResult.Failed -> result.message + " Se conserva el historial local disponible."
                    RefreshResult.Skipped -> "Solo almacenamiento local: no se inició una petición HTTP."
                }
                mutableState.update { it.copy(message = message) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(message = "No fue posible consultar las preferencias para actualizar.") }
            } finally {
                mutableState.update { it.copy(refreshing = false) }
            }
        }
    }

    fun setOnlyMocked(enabled: Boolean) = persistSetting {
        saveSettings.onlyMocked(enabled)
    }

    fun setOnlyLocal(enabled: Boolean) = persistSetting {
        saveSettings.onlyLocal(enabled)
    }

    private fun persistSetting(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                mutableState.update { it.copy(settingsError = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(settingsError = "No fue posible guardar la preferencia.") }
            }
        }
    }
}