package com.example.advancedtracker.domain.remote


enum class RemoteErrorKind { HTTP, CONNECTION, SERIALIZATION, UNEXPECTED }

sealed interface RemoteResult<out T> {
    data class Success<out T>(val data: T) : RemoteResult<T>
    data class Error(
        val kind: RemoteErrorKind,
        val message: String,
        val httpCode: Int? = null
    ) : RemoteResult<Nothing>
}