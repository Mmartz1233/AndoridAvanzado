package com.example.advancedtracker.data.remote.network


import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object RemoteNetworkFactory {
    val json = Json { ignoreUnknownKeys = true }

    fun client(auth: RemoteAuthInterceptor, logs: Boolean): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (logs) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(auth)
            .addInterceptor(logging)
            .build()
    }

    fun api(baseUrl: String, client: OkHttpClient): RemoteTelemetryApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RemoteTelemetryApi::class.java)
}