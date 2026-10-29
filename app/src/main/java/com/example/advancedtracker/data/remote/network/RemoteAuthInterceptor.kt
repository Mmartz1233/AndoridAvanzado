package com.example.advancedtracker.data.remote.network


import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class RemoteAuthInterceptor @Inject constructor(
    private val tokens: RemoteTokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header("Accept", "application/json")
        val token = tokens.token
        if (token.isNullOrBlank()) request.removeHeader("Authorization")
        else request.header("Authorization", "Bearer $token")
        return chain.proceed(request.build())
    }
}