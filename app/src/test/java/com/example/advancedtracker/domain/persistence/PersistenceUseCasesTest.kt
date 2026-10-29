package com.example.advancedtracker.domain.persistence


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class PersistenceUseCasesTest {
    private class Settings : TelemetrySettingsRepository {
        override val settings = MutableStateFlow(TelemetrySettings())
        override suspend fun saveOnlyMocked(enabled: Boolean) {
            settings.value = settings.value.copy(onlyMocked = enabled)
        }
        override suspend fun saveOnlyLocal(enabled: Boolean) {
            settings.value = settings.value.copy(onlyLocal = enabled)
        }
    }

    private class Records : StoredTelemetryRepository {
        val rows = MutableStateFlow<List<TelemetryRecord>>(emptyList())
        var refreshCalls = 0
        override fun observeRecords() = rows
        override suspend fun refresh(): RefreshResult {
            refreshCalls++
            return RefreshResult.Updated(0)
        }
    }

    @Test
    fun localOnlyDoesNotInvokeRefresh() = runTest {
        val records = Records()
        val settings = Settings().apply { saveOnlyLocal(true) }
        assertEquals(RefreshResult.Skipped, RefreshStoredTelemetryUseCase(records, settings)())
        assertEquals(0, records.refreshCalls)
    }

    @Test
    fun filteringUsesSavedMockedPreference() = runTest {
        val records = Records().apply {
            rows.value = listOf(
                TelemetryRecord("a", 19.0, -99.0, 60.0, Date(1L), false),
                TelemetryRecord("b", 20.0, -98.0, 30.0, Date(2L), true)
            )
        }
        val settings = Settings()
        SaveTelemetrySettingsUseCase(settings).onlyMocked(true)
        val result = ObserveStoredTelemetryUseCase(records, settings)().first()
        assertEquals(listOf("b"), result.map { it.id })
    }
}