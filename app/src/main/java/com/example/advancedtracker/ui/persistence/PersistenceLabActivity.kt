package com.example.advancedtracker.ui.persistence


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersistenceLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[PersistenceViewModel::class.java]
        setContent {
            val state by viewModel.state.collectAsState()
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    PersistenceScreen(
                        state = state,
                        onRefresh = viewModel::refresh,
                        onOnlyMocked = { viewModel.setOnlyMocked(it) },
                        onOnlyLocal = { viewModel.setOnlyLocal(it) }
                    )
                }
            }
        }
    }
}