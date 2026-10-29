package com.example.advancedtracker.ui.remote


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.remote.RemoteResult
import com.example.advancedtracker.domain.usecase.remote.FetchRemoteTelemetryUseCase
import com.example.advancedtracker.domain.usecase.remote.SendRemoteTelemetryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RemoteTelemetryViewModel @Inject constructor(
    private val fetch: FetchRemoteTelemetryUseCase,
    private val send: SendRemoteTelemetryUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<RemoteTelemetryUiState>(RemoteTelemetryUiState.Loading)
    val state = _state.asStateFlow()
    private var operation: Job? = null

    init { refresh() }

    fun refresh() {
        if (operation?.isActive == true) return
        operation = viewModelScope.launch {
            _state.value = RemoteTelemetryUiState.Loading
            publish(fetch())
        }
    }

    fun sendDemo() {
        if (operation?.isActive == true) return
        operation = viewModelScope.launch {
            _state.value = RemoteTelemetryUiState.Loading
            val sample = TelemetryData(
                id = UUID.randomUUID().toString(),
                latitude = 19.4326,
                longitude = -99.1332,
                speedKmh = 0.0,
                timestamp = Date(),
                isMocked = true
            )
            when (val result = send(sample)) {
                is RemoteResult.Error -> {
                    _state.value = RemoteTelemetryUiState.Error("No se confirmó el envío. ${result.message}")
                }
                is RemoteResult.Success -> {
                    when (val history = fetch()) {
                        is RemoteResult.Success -> publish(history, "Enviado: ${result.data.id}")
                        is RemoteResult.Error -> _state.value = RemoteTelemetryUiState.Error(
                            "El registro se envió, pero no se pudo consultar la lista. ${history.message}"
                        )
                    }
                }
            }
        }
    }

    private fun publish(result: RemoteResult<List<TelemetryData>>, notice: String? = null) {
        _state.value = when (result) {
            is RemoteResult.Success -> RemoteTelemetryUiState.Success(result.data, notice)
            is RemoteResult.Error -> RemoteTelemetryUiState.Error(result.message)
        }
    }
}