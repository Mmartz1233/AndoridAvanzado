package com.example.advancedtracker.ui.persistence


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PersistenceScreen(
    state: PersistenceUiState,
    onRefresh: () -> Unit,
    onOnlyMocked: (Boolean) -> Unit,
    onOnlyLocal: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Laboratorio 5.1 · Persistencia", style = MaterialTheme.typography.titleLarge)
        Text("Datos mostrados desde Room: ${state.records.size}")
        Text("Filtro guardado — solo simulados: ${state.settings.onlyMocked}")
        Text("Solo local guardado: ${state.settings.onlyLocal}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Mostrar solo lecturas simuladas", modifier = Modifier.weight(1f))
            Switch(
                checked = state.settings.onlyMocked,
                onCheckedChange = onOnlyMocked,
                enabled = state.settingsReady
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Consultar solo almacenamiento local", modifier = Modifier.weight(1f))
            Switch(
                checked = state.settings.onlyLocal,
                onCheckedChange = onOnlyLocal,
                enabled = state.settingsReady && !state.refreshing
            )
        }
        Button(onClick = onRefresh, enabled = state.settingsReady && !state.refreshing) {
            Text("Actualizar")
        }
        if (state.reading || state.refreshing) CircularProgressIndicator()
        if (state.message.isNotBlank()) Text(state.message)
        state.storageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.settingsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (!state.reading && state.records.isEmpty() && state.storageError == null) {
            Text("Sin registros para el filtro actual. Room puede estar vacío o no contener lecturas simuladas.")
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.records, key = { it.id }) { record ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(record.id)
                    Text("Latitud: ${record.latitude} · Longitud: ${record.longitude}")
                    Text("Velocidad: ${record.speedKmh} km/h · Simulado: ${record.isMocked}")
                    Text("Fecha: ${record.timestamp}")
                    Text("Timestamp en milisegundos: ${record.timestamp.time}")
                }
                HorizontalDivider()
            }
        }
    }
}