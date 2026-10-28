// MainActivity.kt
package com.example.advancedtracker

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import com.example.advancedtracker.ui.telemetry.TelemetryScreen
import com.example.advancedtracker.ui.theme.AdvancedTrackerTheme

import dagger.hilt.android.AndroidEntryPoint

/**
 * Punto de entrada visual del ciclo de vida de Android.
 * La anotación @AndroidEntryPoint es requerida por Hilt para la inyección en componentes del sistema.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdvancedTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TelemetryScreen()
                }
            }
        }
    }
}