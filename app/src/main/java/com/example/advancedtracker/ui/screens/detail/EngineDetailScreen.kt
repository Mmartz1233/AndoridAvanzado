package com.example.advancedtracker.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.advancedtracker.domain.model.TelemetryEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineDetailScreen(
    engine: TelemetryEngine?,
    onNavigateBack: () -> Unit,
    onSaveMetrics: (temperature: Double, rpm: Int, fuel: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(engine?.name ?: "Buscando Motor...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (engine == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Uso obligatorio de rememberSaveable para sobrevivir a recreaciones de Activity/Rotaciones
            var inputTemp by rememberSaveable { mutableStateOf(engine.coreTemperature.toString()) }
            var inputRpm by rememberSaveable { mutableStateOf(engine.currentRpm.toString()) }
            var inputFuel by rememberSaveable { mutableStateOf(engine.fuelLevel.toString()) }

            var isError by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ESTADO DE RIESGO: ${engine.failureRisk}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (engine.failureRisk > 50.0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Modelo de turbina validado: ${engine.model}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider()

                Text(
                    text = "Panel de Modulación Física (Simulador)",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = inputTemp,
                    onValueChange = { inputTemp = it },
                    label = { Text("Temperatura de Núcleo (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )

                OutlinedTextField(
                    value = inputRpm,
                    onValueChange = { inputRpm = it },
                    label = { Text("Velocidad de Operación (RPM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )

                OutlinedTextField(
                    value = inputFuel,
                    onValueChange = { inputFuel = it },
                    label = { Text("Nivel de Carburante (0.0 a 1.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError
                )

                if (isError) {
                    Text(
                        text = "Datos de entrada incorrectos. Asegúrese de ingresar valores numéricos válidos.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        val parsedTemp = inputTemp.toDoubleOrNull()
                        val parsedRpm = inputRpm.toIntOrNull()
                        val parsedFuel = inputFuel.toFloatOrNull()

                        if (parsedTemp != null && parsedRpm != null && parsedFuel != null && parsedFuel in 0.0f..1.0f) {
                            isError = false
                            onSaveMetrics(parsedTemp, parsedRpm, parsedFuel)
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar Consola de Simulación")
                }
            }
        }
    }
}