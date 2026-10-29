package com.example.advancedtracker.data.repository.persistence

import com.example.advancedtracker.data.local.persistence.TelemetryDao
import com.example.advancedtracker.data.local.persistence.TelemetryEntity
import com.example.advancedtracker.data.remote.dto.RemoteTelemetryDto
import com.example.advancedtracker.data.remote.network.RemoteTelemetryApi
import com.example.advancedtracker.domain.persistence.FailureKind
import com.example.advancedtracker.domain.persistence.RefreshResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class RoomTelemetryRepositoryTest {
    private fun dto(id: String = "tel_01", speedKmh: Double = 62.5) =
        RemoteTelemetryDto(id, 19.4326, -99.1332, speedKmh, 1700000000000L, false)

    private class FakeApi : RemoteTelemetryApi {
        var response = emptyList<RemoteTelemetryDto>()
        var failure: Exception? = null
        var calls = 0

        override suspend fun fetchHistory(limit: Int): List<RemoteTelemetryDto> {
            calls++
            failure?.let { throw it }
            return response
        }

        override suspend fun sendRecord(data: RemoteTelemetryDto): RemoteTelemetryDto = data
    }

    private class FakeDao : TelemetryDao {
        val rows = MutableStateFlow<List<TelemetryEntity>>(emptyList())
        var writeFailure: Exception? = null
        var readFailure: Exception? = null

        override fun observeAll(): Flow<List<TelemetryEntity>> =
            if (readFailure == null) rows else flow { throw readFailure!! }

        override fun observeByMocked(isMocked: Boolean): Flow<List<TelemetryEntity>> = flow {
            emit(rows.value.filter { it.isMocked == isMocked })
        }

        override suspend fun readAll(): List<TelemetryEntity> = rows.value

        override suspend fun upsertAll(records: List<TelemetryEntity>) {
            writeFailure?.let { throw it }
            rows.value = (rows.value.associateBy { it.id } + records.associateBy { it.id })
                .values.sortedWith(compareByDescending<TelemetryEntity> { it.timestamp }.thenBy { it.id })
        }

        override suspend fun mergeRemote(records: List<TelemetryEntity>) = upsertAll(records)
    }

    @Test
    fun refreshStoresAllSixFields() = runTest {
        val api = FakeApi().apply { response = listOf(dto()) }
        val repository = RoomTelemetryRepository(api, FakeDao())
        assertEquals(RefreshResult.Updated(1), repository.refresh())
        val record = repository.observeRecords().first().single()
        assertEquals("tel_01", record.id)
        assertEquals(19.4326, record.latitude, 0.0)
        assertEquals(-99.1332, record.longitude, 0.0)
        assertEquals(62.5, record.speedKmh, 0.0)
        assertEquals(1700000000000L, record.timestamp.time)
        assertFalse(record.isMocked)
    }

    @Test
    fun observingCacheDoesNotCallApi() = runTest {
        val api = FakeApi()
        val repository = RoomTelemetryRepository(api, FakeDao())
        assertTrue(repository.observeRecords().first().isEmpty())
        assertEquals(0, api.calls)
    }

    @Test
    fun networkFailureKeepsCachedRows() = runTest {
        val api = FakeApi().apply { response = listOf(dto()) }
        val repository = RoomTelemetryRepository(api, FakeDao())
        repository.refresh()
        val before = repository.observeRecords().first()
        api.failure = IOException("Sin conexión")
        val result = repository.refresh() as RefreshResult.Failed
        assertEquals(FailureKind.NETWORK, result.kind)
        assertEquals(before, repository.observeRecords().first())
    }

    @Test
    fun partialOrEmptyResponseDoesNotDeleteHistory() = runTest {
        val api = FakeApi().apply { response = listOf(dto("old"), dto("new")) }
        val repository = RoomTelemetryRepository(api, FakeDao())
        repository.refresh()
        api.response = listOf(dto("new", speedKmh = 80.0))
        repository.refresh()
        assertEquals(setOf("old", "new"), repository.observeRecords().first().map { it.id }.toSet())
        assertEquals(80.0, repository.observeRecords().first().first { it.id == "new" }.speedKmh, 0.0)
        api.response = emptyList()
        repository.refresh()
        assertEquals(2, repository.observeRecords().first().size)
    }

    @Test
    fun writeFailureReturnsLocalStorageError() = runTest {
        val dao = FakeDao().apply { writeFailure = IllegalStateException("Disco no disponible") }
        val api = FakeApi().apply { response = listOf(dto()) }
        val result = RoomTelemetryRepository(api, dao).refresh() as RefreshResult.Failed
        assertEquals(FailureKind.LOCAL_STORAGE, result.kind)
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun readFailureIsNotInventedAsSuccessfulData() = runTest {
        val dao = FakeDao().apply { readFailure = IllegalStateException("Lectura imposible") }
        val repository = RoomTelemetryRepository(FakeApi(), dao)
        try {
            repository.observeRecords().first()
            fail("La lectura debía fallar")
        } catch (expected: IllegalStateException) {
            assertEquals("Lectura imposible", expected.message)
        }
    }

    @Test
    fun cancellationIsPropagated() = runTest {
        val api = FakeApi().apply { failure = CancellationException("Cancelada") }
        try {
            RoomTelemetryRepository(api, FakeDao()).refresh()
            fail("La cancelación debía propagarse")
        } catch (expected: CancellationException) {
            assertEquals("Cancelada", expected.message)
        }
    }
}