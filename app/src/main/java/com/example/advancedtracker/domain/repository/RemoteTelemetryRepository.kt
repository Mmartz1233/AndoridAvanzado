package com.example.advancedtracker.domain.repository


import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.remote.RemoteResult

interface RemoteTelemetryRepository {
    suspend fun fetchHistory(): RemoteResult<List<TelemetryData>>
    suspend fun sendRecord(data: TelemetryData): RemoteResult<TelemetryData>
}