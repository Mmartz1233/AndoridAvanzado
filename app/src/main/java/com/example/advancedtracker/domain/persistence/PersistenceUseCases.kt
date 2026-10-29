package com.example.advancedtracker.domain.persistence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ObserveStoredTelemetryUseCase(
    private val records: StoredTelemetryRepository,
    private val settings: TelemetrySettingsRepository
) {
    operator fun invoke(): Flow<List<TelemetryRecord>> = combine(
        records.observeRecords(), settings.settings
    ) { all, preferences ->
        if (preferences.onlyMocked) all.filter { it.isMocked } else all
    }
}

class ObserveTelemetrySettingsUseCase(
    private val repository: TelemetrySettingsRepository
) {
    operator fun invoke(): Flow<TelemetrySettings> = repository.settings
}

class RefreshStoredTelemetryUseCase(
    private val records: StoredTelemetryRepository,
    private val settings: TelemetrySettingsRepository
) {
    suspend operator fun invoke(): RefreshResult {
        if (settings.settings.first().onlyLocal) return RefreshResult.Skipped
        return records.refresh()
    }
}

class SaveTelemetrySettingsUseCase(
    private val repository: TelemetrySettingsRepository
) {
    suspend fun onlyMocked(enabled: Boolean) = repository.saveOnlyMocked(enabled)
    suspend fun onlyLocal(enabled: Boolean) = repository.saveOnlyLocal(enabled)
}