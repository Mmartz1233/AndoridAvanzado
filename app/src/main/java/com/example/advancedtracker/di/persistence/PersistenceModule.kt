package com.example.advancedtracker.di.persistence

import android.content.Context
import androidx.room.Room
import com.example.advancedtracker.data.local.persistence.TelemetryDao

import com.example.advancedtracker.domain.persistence.ObserveStoredTelemetryUseCase
import com.example.advancedtracker.domain.persistence.ObserveTelemetrySettingsUseCase
import com.example.advancedtracker.domain.persistence.RefreshStoredTelemetryUseCase
import com.example.advancedtracker.domain.persistence.SaveTelemetrySettingsUseCase
import com.example.advancedtracker.domain.persistence.StoredTelemetryRepository
import com.example.advancedtracker.domain.persistence.TelemetrySettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.advancedtracker.data.local.persistence.MIGRATION_1_2
import com.example.advancedtracker.data.local.persistence.TrackingTelemetryDatabase

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Provides
    @Singleton

    fun provideDatabase(
        @ApplicationContext context: Context
    ): TrackingTelemetryDatabase =
        Room.databaseBuilder(
            context,
            TrackingTelemetryDatabase::class.java,
            "telemetry_lab51_tracking_db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideDao(
        database: TrackingTelemetryDatabase
    ): TelemetryDao = database.telemetryDao()
    /**
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TelemetryDatabase =
        Room.databaseBuilder(
            context,
            TelemetryDatabase::class.java,
            "telemetry_lab51_tracking_db"
        ).build()

    @Provides
    fun provideDao(
        database: TelemetryDatabase
    ): TelemetryDao = database.telemetryDao()
**/
    @Provides
    fun provideObserveRecords(
        records: StoredTelemetryRepository,
        settings: TelemetrySettingsRepository
    ): ObserveStoredTelemetryUseCase =
        ObserveStoredTelemetryUseCase(records, settings)

    @Provides
    fun provideObserveSettings(
        repository: TelemetrySettingsRepository
    ): ObserveTelemetrySettingsUseCase =
        ObserveTelemetrySettingsUseCase(repository)

    @Provides
    fun provideRefresh(
        records: StoredTelemetryRepository,
        settings: TelemetrySettingsRepository
    ): RefreshStoredTelemetryUseCase =
        RefreshStoredTelemetryUseCase(records, settings)

    @Provides
    fun provideSaveSettings(
        repository: TelemetrySettingsRepository
    ): SaveTelemetrySettingsUseCase =
        SaveTelemetrySettingsUseCase(repository)
}
