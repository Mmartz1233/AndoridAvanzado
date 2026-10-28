package com.example.advancedtracker.ui.telemetry


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TelemetryScreen(
    modifier: Modifier = Modifier,
    viewModel: TelemetryViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Recolectamos el estado de manera segura para evitar fugas y consumo innecesario de recursos
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Escuchamos de manera segura los eventos asíncronos puntuales mediante LaunchedEffect
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is TelemetryUiEvent.ShowToastMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is TelemetryUiEvent.NavigateToDetails -> {
                    Toast.makeText(context, "Navegando al detalle de telemetría: ${event.telemetryId}", Toast.LENGTH_SHORT).show()
                    // Aquí iría el controlador de navegación real (NavController)
                }
                is TelemetryUiEvent.NavigationBack -> {
                    // Acción de retroceso
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Telemetría en Tiempo Real",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Visualización del estado actual de geolocalización
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val telemetry = uiState.currentTelemetry
                    if (telemetry != null) {
                        Text(text = "ID: ${telemetry.id}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Latitud: ${telemetry.latitude}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Longitud: ${telemetry.longitude}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Velocidad: ${telemetry.speedKmh} Km/H",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Fecha: ${telemetry.timestamp}", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(
                            text = "Buscando coordenadas satelitales activas...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bloque de botones de interacción del usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (uiState.isTrackingActive) {
                    Button(
                        onClick = { viewModel.stopTracking() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Pausar GPS")
                    }
                } else {
                    Button(onClick = { viewModel.startTracking() }) {
                        Text("Iniciar GPS")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.persistCurrentTelemetry() },
                    enabled = uiState.currentTelemetry != null
                ) {
                    Text("Guardar Lectura")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.navigateToTelemetryDetail() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                enabled = uiState.currentTelemetry != null
            ) {
                Text("Ver Detalles Completos")
            }
        }
    }
}