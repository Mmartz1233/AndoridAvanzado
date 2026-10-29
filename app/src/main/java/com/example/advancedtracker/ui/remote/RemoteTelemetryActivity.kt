package com.example.advancedtracker.ui.remote


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemoteTelemetryActivity : ComponentActivity() {
    private val viewModel: RemoteTelemetryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                Scaffold { padding ->
                    RemoteTelemetryScreen(
                        state = state,
                        onRefresh = viewModel::refresh,
                        onSend = viewModel::sendDemo,
                        onBack = { finish() },
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}