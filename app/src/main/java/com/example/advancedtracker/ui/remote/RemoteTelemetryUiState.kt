package com.example.advancedtracker.ui.remote


import com.example.advancedtracker.domain.model.tracking.TelemetryData

sealed interface RemoteTelemetryUiState {
    data object Loading : RemoteTelemetryUiState
    data class Success(val records: List<TelemetryData>, val notice: String? = null) : RemoteTelemetryUiState
    data class Error(val message: String) : RemoteTelemetryUiState
}