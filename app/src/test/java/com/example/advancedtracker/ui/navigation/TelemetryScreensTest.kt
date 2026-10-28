package com.example.advancedtracker.ui.navigation
import org.junit.Assert.assertEquals
import org.junit.Test

class TelemetryScreensTest {

    @Test
    fun lasRutasTienenSerializador() {
        EngineListDestination.serializer()

        val descriptor = EngineDetailDestination.serializer().descriptor
        assertEquals(1, descriptor.elementsCount)
        assertEquals("engineId", descriptor.getElementName(0))

        // Si falta el complemento de serialización, estas llamadas no compilarán.
        val listDescriptor = EngineListDestination.serializer().descriptor
        val detailDescriptor = EngineDetailDestination.serializer().descriptor

        // La lista no requiere argumentos; el detalle recibe engineId.
        assertEquals(0, listDescriptor.elementsCount)
        assertEquals(1, detailDescriptor.elementsCount)
        assertEquals("engineId", detailDescriptor.getElementName(0))
    }
}