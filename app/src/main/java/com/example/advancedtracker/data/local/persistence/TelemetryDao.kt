package com.example.advancedtracker.data.local.persistence

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry_table ORDER BY timestamp DESC, id ASC")
    fun observeAll(): Flow<List<TelemetryEntity>>

    @Query("SELECT * FROM telemetry_table WHERE is_mocked = :isMocked ORDER BY timestamp DESC, id ASC")
    fun observeByMocked(isMocked: Boolean): Flow<List<TelemetryEntity>>

    @Query("SELECT * FROM telemetry_table ORDER BY timestamp DESC, id ASC")
    suspend fun readAll(): List<TelemetryEntity>

    @Upsert
    suspend fun upsertAll(records: List<TelemetryEntity>)

    @Transaction
    suspend fun mergeRemote(records: List<TelemetryEntity>) {
        upsertAll(records)
    }
}