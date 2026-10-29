package com.example.advancedtracker.ui.remote


import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.advancedtracker.BuildConfig

@Composable
fun OpenRemoteTelemetryButton() {
    if (BuildConfig.DEBUG) {
        val context = LocalContext.current
        Button(onClick = {
            context.startActivity(Intent(context, RemoteTelemetryActivity::class.java))
        }) {
            Text("Abrir telemetría REST")
        }
    }
}