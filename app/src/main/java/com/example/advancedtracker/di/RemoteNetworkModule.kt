package com.example.advancedtracker.di

import com.example.advancedtracker.BuildConfig
import com.example.advancedtracker.data.remote.network.RemoteAuthInterceptor
import com.example.advancedtracker.data.remote.network.RemoteNetworkFactory
import com.example.advancedtracker.data.remote.network.RemoteTelemetryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteHttpClient

@Module
@InstallIn(SingletonComponent::class)
object RemoteNetworkModule {
    private const val BASE_URL = "http://10.0.2.2:8080/v1/"

    @Provides
    @Singleton
    @RemoteHttpClient
    fun provideClient(auth: RemoteAuthInterceptor): OkHttpClient =
        RemoteNetworkFactory.client(auth, logs = BuildConfig.DEBUG)

    @Provides
    @Singleton
    fun provideRemoteApi(@RemoteHttpClient client: OkHttpClient): RemoteTelemetryApi =
        RemoteNetworkFactory.api(BASE_URL, client)
}