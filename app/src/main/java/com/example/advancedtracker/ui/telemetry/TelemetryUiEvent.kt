package com.example.advancedtracker.ui.telemetry
// TelemetryUiEvent.kt

/**
 * Eventos no persistentes orientados a disparar acciones transitorias de interfaz
 * como alertas temporales (Toasts, Snackbars) o navegación a otras pantallas.
 */
sealed interface TelemetryUiEvent {
    data class ShowToastMessage(val message: String) : TelemetryUiEvent
    data class NavigateToDetails(val telemetryId: String) : TelemetryUiEvent
    object NavigationBack : TelemetryUiEvent
}