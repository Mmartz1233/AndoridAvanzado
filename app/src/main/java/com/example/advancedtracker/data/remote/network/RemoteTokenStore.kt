package com.example.advancedtracker.data.remote.network


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteTokenStore @Inject constructor() {
    @Volatile
    var token: String? = "lab-token"
}