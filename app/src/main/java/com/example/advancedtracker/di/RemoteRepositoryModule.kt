package com.example.advancedtracker.di


import com.example.advancedtracker.data.repository.RemoteTelemetryRepositoryImpl
import com.example.advancedtracker.domain.repository.RemoteTelemetryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteRepositoryModule {
    @Binds
    abstract fun bindRemoteRepository(implementation: RemoteTelemetryRepositoryImpl): RemoteTelemetryRepository
}