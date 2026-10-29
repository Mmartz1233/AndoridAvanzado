package com.example.advancedtracker.domain.persistence

import kotlinx.coroutines.flow.Flow

interface TelemetrySettingsRepository {
    val settings: Flow<TelemetrySettings>
    suspend fun saveOnlyMocked(enabled: Boolean)
    suspend fun saveOnlyLocal(enabled: Boolean)
}