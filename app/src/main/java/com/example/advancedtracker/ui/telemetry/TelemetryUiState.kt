package com.example.advancedtracker.ui.telemetry

import com.example.advancedtracker.domain.model.tracking.TelemetryData

/**
 * Representación inmutable del estado completo de la interfaz visual.
 */
data class TelemetryUiState(
    val isLoading: Boolean = false,
    val currentTelemetry: TelemetryData? = null,
    val lastError: String? = null,
    val isTrackingActive: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)