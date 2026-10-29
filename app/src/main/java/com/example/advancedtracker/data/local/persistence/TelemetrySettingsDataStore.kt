package com.example.advancedtracker.data.local.persistence

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.advancedtracker.domain.persistence.TelemetrySettings
import com.example.advancedtracker.domain.persistence.TelemetrySettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.telemetryLabSettings by preferencesDataStore(
    name = "telemetry_lab51_settings"
)

@Singleton
class TelemetrySettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : TelemetrySettingsRepository {
    private val onlyMocked = booleanPreferencesKey("only_mocked")
    private val onlyLocal = booleanPreferencesKey("only_local")

    override val settings: Flow<TelemetrySettings> =
        context.telemetryLabSettings.data.map { preferences ->
            TelemetrySettings(
                onlyMocked = preferences[onlyMocked] ?: false,
                onlyLocal = preferences[onlyLocal] ?: false
            )
        }

    override suspend fun saveOnlyMocked(enabled: Boolean) {
        context.telemetryLabSettings.edit { preferences -> preferences[onlyMocked] = enabled }
    }

    override suspend fun saveOnlyLocal(enabled: Boolean) {
        context.telemetryLabSettings.edit { preferences -> preferences[onlyLocal] = enabled }
    }
}