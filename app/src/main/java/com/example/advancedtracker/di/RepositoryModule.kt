package com.example.advancedtracker.di


import com.example.advancedtracker.data.repository.TelemetryRepositoryImpl
import com.example.advancedtracker.domain.repository.TelemetryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt encargado de proveer de forma inyectable la abstracción de Repositorio.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTelemetryRepository(
        telemetryRepositoryImpl: TelemetryRepositoryImpl
    ): TelemetryRepository
}