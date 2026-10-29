package com.example.advancedtracker.di.persistence

import com.example.advancedtracker.data.local.persistence.TelemetrySettingsDataStore
import com.example.advancedtracker.data.repository.persistence.RoomTelemetryRepository
import com.example.advancedtracker.domain.persistence.StoredTelemetryRepository
import com.example.advancedtracker.domain.persistence.TelemetrySettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PersistenceBindings {
    @Binds
    abstract fun bindStoredRecords(impl: RoomTelemetryRepository): StoredTelemetryRepository

    @Binds
    abstract fun bindSettings(impl: TelemetrySettingsDataStore): TelemetrySettingsRepository
}