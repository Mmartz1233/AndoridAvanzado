package com.example.advancedtracker.data.repository

import com.example.advancedtracker.data.remote.dto.toRemoteDto
import com.example.advancedtracker.data.remote.dto.toTelemetryData
import com.example.advancedtracker.data.remote.network.RemoteTelemetryApi
import com.example.advancedtracker.data.remote.network.safeRemoteCall
import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.remote.RemoteResult
import com.example.advancedtracker.domain.repository.RemoteTelemetryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteTelemetryRepositoryImpl @Inject constructor(
    private val api: RemoteTelemetryApi
) : RemoteTelemetryRepository {
    override suspend fun fetchHistory(): RemoteResult<List<TelemetryData>> =
        safeRemoteCall { api.fetchHistory().map { it.toTelemetryData() } }

    override suspend fun sendRecord(data: TelemetryData): RemoteResult<TelemetryData> =
        safeRemoteCall { api.sendRecord(data.toRemoteDto()).toTelemetryData() }
}