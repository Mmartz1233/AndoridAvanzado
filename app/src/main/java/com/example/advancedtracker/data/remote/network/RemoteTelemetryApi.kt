package com.example.advancedtracker.data.remote.network

import com.example.advancedtracker.data.remote.dto.RemoteTelemetryDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RemoteTelemetryApi {
    @GET("telemetry")
    suspend fun fetchHistory(@Query("limit") limit: Int = 50): List<RemoteTelemetryDto>

    @POST("telemetry")
    suspend fun sendRecord(@Body data: RemoteTelemetryDto): RemoteTelemetryDto
}