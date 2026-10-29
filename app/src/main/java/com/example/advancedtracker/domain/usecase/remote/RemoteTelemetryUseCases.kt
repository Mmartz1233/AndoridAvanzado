package com.example.advancedtracker.domain.usecase.remote

import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.repository.RemoteTelemetryRepository
import javax.inject.Inject

class FetchRemoteTelemetryUseCase @Inject constructor(
    private val repository: RemoteTelemetryRepository
) {
    suspend operator fun invoke() = repository.fetchHistory()
}

class SendRemoteTelemetryUseCase @Inject constructor(
    private val repository: RemoteTelemetryRepository
) {
    suspend operator fun invoke(data: TelemetryData) = repository.sendRecord(data)
}