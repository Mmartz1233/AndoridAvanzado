package com.example.advancedtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de la aplicación que desencadena la generación de código de Hilt.
 */
@HiltAndroidApp
class TrackerApplication : Application()