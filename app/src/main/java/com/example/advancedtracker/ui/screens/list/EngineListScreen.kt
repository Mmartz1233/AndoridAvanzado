package com.example.advancedtracker.ui.screens.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.advancedtracker.domain.model.EngineStatus
import com.example.advancedtracker.domain.model.TelemetryEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineListScreen(
    engines: List<TelemetryEngine>,
    onEngineClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredEngines = remember(engines, searchQuery) {
        if (searchQuery.isBlank()) {
            engines
        } else {
            engines.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telemetría de Motores") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Buscador dinámico de motores
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Filtrar por nombre o ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            if (filteredEngines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron motores.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredEngines,
                        key = { it.id } // Optimización crucial para reconciliación en LazyColumn
                    ) { engine ->
                        EngineItemCard(
                            engine = engine,
                            onClick = { onEngineClick(engine.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngineItemCard(
    engine: TelemetryEngine,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = engine.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ID: ${engine.id} | Modelo: ${engine.model}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { engine.fuelLevel },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = engine.status)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${engine.coreTemperature}°C",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (engine.status == EngineStatus.CRITICAL_OVERHEAT) {
                        Color.Red
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: EngineStatus) {
    val (color, text) = when (status) {
        EngineStatus.ACTIVE -> Color(0xFF2E7D32) to "ACTIVE"
        EngineStatus.STANDBY -> Color(0xFFEF6C00) to "STANDBY"
        EngineStatus.CRITICAL_OVERHEAT -> Color(0xFFC62828) to "CRITICAL"
        EngineStatus.MAINTENANCE -> Color(0xFF1565C0) to "SERVICE"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}