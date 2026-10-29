package com.example.advancedtracker.domain.persistence


import kotlinx.coroutines.flow.Flow

interface StoredTelemetryRepository {
    fun observeRecords(): Flow<List<TelemetryRecord>>
    suspend fun refresh(): RefreshResult
}