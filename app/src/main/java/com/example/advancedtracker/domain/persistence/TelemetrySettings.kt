package com.example.advancedtracker.domain.persistence


data class TelemetrySettings(
    val onlyMocked: Boolean = false,
    val onlyLocal: Boolean = false
)