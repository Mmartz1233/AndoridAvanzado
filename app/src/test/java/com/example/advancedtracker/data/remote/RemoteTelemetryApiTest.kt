package com.example.advancedtracker.data.remote


import com.example.advancedtracker.data.remote.dto.toRemoteDto
import com.example.advancedtracker.data.remote.dto.toTelemetryData
import com.example.advancedtracker.data.remote.network.RemoteAuthInterceptor
import com.example.advancedtracker.data.remote.network.RemoteNetworkFactory
import com.example.advancedtracker.data.remote.network.RemoteTokenStore
import com.example.advancedtracker.data.remote.network.safeRemoteCall

import com.example.advancedtracker.data.repository.RemoteTelemetryRepositoryImpl


import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.remote.RemoteErrorKind
import com.example.advancedtracker.domain.remote.RemoteResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.concurrent.TimeUnit

class RemoteTelemetryApiTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var tokens: RemoteTokenStore
    private lateinit var repository: RemoteTelemetryRepositoryImpl
    private val itemJson = """
        {"id":"tel_01","latitude":19.4326,"longitude":-99.1332,
         "speedKmh":62.5,"timestamp":1700000000000,"isMocked":true}
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokens = RemoteTokenStore()
        client = RemoteNetworkFactory.client(RemoteAuthInterceptor(tokens), logs = false)
        repository = RemoteTelemetryRepositoryImpl(
            RemoteNetworkFactory.api(server.url("/v1/").toString(), client)
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
        client.connectionPool.evictAll()
        client.dispatcher.executorService.shutdown()
    }

    private fun respond(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code)
            .setHeader("Content-Type", "application/json").setBody(body))
    }

    @Test
    fun getMapsExistingModelAndSendsBearer() = runBlocking {
        respond(200, "[$itemJson]")
        val result = repository.fetchHistory()
        assertTrue(result is RemoteResult.Success)
        val list = (result as RemoteResult.Success).data
        assertEquals(1, list.size)
        assertEquals("tel_01", list[0].id)
        assertEquals(62.5, list[0].speedKmh, 0.0001)
        assertEquals(1700000000000L, list[0].timestamp.time)
        assertTrue(list[0].isMocked)
        val request = server.takeRequest(1, TimeUnit.SECONDS)
        assertNotNull(request)
        assertEquals("GET", request!!.method)
        assertEquals("/v1/telemetry?limit=50", request.path)
        assertEquals("Bearer lab-token", request.getHeader("Authorization"))
    }

    @Test
    fun postPreservesIdDateAndExistingFields() = runBlocking {
        respond(201, itemJson)
        val sample = TelemetryData(id = "tel_01", latitude = 19.4326, longitude = -99.1332,
            speedKmh = 62.5, timestamp = Date(1700000000000L), isMocked = true)
        val result = repository.sendRecord(sample)
        assertTrue(result is RemoteResult.Success)
        assertEquals(sample.id, (result as RemoteResult.Success).data.id)
        val request = server.takeRequest(1, TimeUnit.SECONDS)
        assertNotNull(request)
        assertEquals("POST", request!!.method)
        assertEquals("/v1/telemetry", request.path)
        val body = RemoteNetworkFactory.json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals(sample.id, body["id"]!!.jsonPrimitive.content)
        assertEquals(sample.timestamp.time.toString(), body["timestamp"]!!.jsonPrimitive.content)
        assertTrue(body.containsKey("speedKmh"))
        assertTrue(body.containsKey("isMocked"))
        assertFalse(body.containsKey("deviceId"))
        assertFalse(body.containsKey("status"))
    }

    @Test
    fun absentTokenIsOmittedAnd401IsReported() = runBlocking {
        tokens.token = null
        respond(401, "{}")
        val result = repository.fetchHistory()
        assertTrue(result is RemoteResult.Error)
        assertEquals(401, (result as RemoteResult.Error).httpCode)
        val request = server.takeRequest(1, TimeUnit.SECONDS)
        assertNotNull(request)
        assertNull(request!!.getHeader("Authorization"))
    }

    @Test
    fun http500IsPreserved() = runBlocking {
        respond(500, "{}")
        val result = repository.fetchHistory()
        assertTrue(result is RemoteResult.Error)
        val error = result as RemoteResult.Error
        assertEquals(RemoteErrorKind.HTTP, error.kind)
        assertEquals(500, error.httpCode)
    }

    @Test
    fun http404IsPreserved() = runBlocking {
        respond(404, "{}")
        val result = repository.fetchHistory()
        assertTrue(result is RemoteResult.Error)
        assertEquals(404, (result as RemoteResult.Error).httpCode)
    }

    @Test
    fun incompleteJsonIsSerializationError() = runBlocking {
        respond(200, "[{\"id\":\"incompleto\"}]")
        val result = repository.fetchHistory()
        assertTrue(result is RemoteResult.Error)
        assertEquals(RemoteErrorKind.SERIALIZATION, (result as RemoteResult.Error).kind)
    }

    @Test
    fun emptyArrayIsSuccessful() = runBlocking {
        respond(200, "[]")
        val result = repository.fetchHistory()
        assertTrue(result is RemoteResult.Success)
        assertTrue((result as RemoteResult.Success).data.isEmpty())
    }

    @Test
    fun dtoRoundTripPreservesOriginalModelValues() {
        val sample = TelemetryData(id = "roundtrip", latitude = -33.4489, longitude = -70.6693,
            speedKmh = 80.0, timestamp = Date(1700000000123L), isMocked = true)
        val reconstructed = sample.toRemoteDto().toTelemetryData()
        assertEquals(sample.id, reconstructed.id)
        assertEquals(sample.latitude, reconstructed.latitude, 0.0)
        assertEquals(sample.longitude, reconstructed.longitude, 0.0)
        assertEquals(sample.speedKmh, reconstructed.speedKmh, 0.0)
        assertEquals(sample.timestamp.time, reconstructed.timestamp.time)
        assertEquals(sample.isMocked, reconstructed.isMocked)
    }

    @Test
    fun ioExceptionIsConnectionError() = runBlocking {
        val result = safeRemoteCall<Unit> { throw IOException("simulado") }
        assertTrue(result is RemoteResult.Error)
        assertEquals(RemoteErrorKind.CONNECTION, (result as RemoteResult.Error).kind)
    }

    @Test
    fun cancellationIsRethrown() = runBlocking {
        val cancellation = CancellationException("prueba")
        try {
            safeRemoteCall<Unit> { throw cancellation }
            fail("La cancelación no debe transformarse en Error")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }
}