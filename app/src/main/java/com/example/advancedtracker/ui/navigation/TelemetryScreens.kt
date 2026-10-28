package com.example.advancedtracker.ui.navigation


import kotlinx.serialization.Serializable

@Serializable
object EngineListDestination

@Serializable
data class EngineDetailDestination(val engineId: String)