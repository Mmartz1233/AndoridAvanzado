package com.example.advancedtracker.domain.persistence


sealed interface RefreshResult {
    data class Updated(val receivedCount: Int) : RefreshResult
    data class Failed(val kind: FailureKind, val message: String) : RefreshResult
    data object Skipped : RefreshResult
}

enum class FailureKind { NETWORK, LOCAL_STORAGE }