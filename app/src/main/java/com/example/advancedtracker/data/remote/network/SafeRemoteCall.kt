package com.example.advancedtracker.data.remote.network



import com.example.advancedtracker.domain.remote.RemoteErrorKind
import com.example.advancedtracker.domain.remote.RemoteResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeRemoteCall(call: suspend () -> T): RemoteResult<T> = try {
    RemoteResult.Success(call())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    val message = when (e.code()) {
        401 -> "La API rechazó el token de práctica."
        404 -> "El recurso no existe."
        409 -> "El id ya existe con otros datos."
        in 500..599 -> "El servidor no pudo completar la operación."
        else -> "La solicitud falló: HTTP ${e.code()}."
    }
    RemoteResult.Error(RemoteErrorKind.HTTP, message, e.code())
} catch (e: SerializationException) {
    RemoteResult.Error(RemoteErrorKind.SERIALIZATION, "JSON incompatible con la telemetría esperada.")
} catch (e: IOException) {
    RemoteResult.Error(RemoteErrorKind.CONNECTION, "No fue posible conectar con la API local.")
} catch (e: Exception) {
    RemoteResult.Error(RemoteErrorKind.UNEXPECTED, "Error inesperado en la operación remota.")
}