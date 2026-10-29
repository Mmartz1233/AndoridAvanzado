package com.example.advancedtracker.ui.remote


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RemoteTelemetryScreen(
    state: RemoteTelemetryUiState,
    onRefresh: () -> Unit,
    onSend: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Telemetría REST", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onBack) { Text("Volver") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh, enabled = state !is RemoteTelemetryUiState.Loading) {
                Text("Consultar")
            }
            Button(onClick = onSend, enabled = state is RemoteTelemetryUiState.Success) {
                Text("Enviar demo")
            }
        }
        when (state) {
            RemoteTelemetryUiState.Loading -> {
                CircularProgressIndicator()
                Text("Operación de red en curso…")
            }
            is RemoteTelemetryUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = onRefresh) { Text("Reintentar consulta") }
            }
            is RemoteTelemetryUiState.Success -> {
                state.notice?.let { Text(it) }
                Text("Registros: ${state.records.size}")
                if (state.records.isEmpty()) Text("No hay registros de telemetría.")
                else LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.records, key = { it.id }) { data ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Id: ${data.id}")
                                Text("Latitud: ${data.latitude}")
                                Text("Longitud: ${data.longitude}")
                                Text("Velocidad: ${data.speedKmh} km/h")
                                Text("Fecha: ${data.timestamp}")
                                Text("Demostración: ${data.isMocked}")
                            }
                        }
                    }
                }
            }
        }
    }
}