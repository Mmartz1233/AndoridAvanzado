package com.example.advancedtracker.data.repository.persistence

import com.example.advancedtracker.data.local.persistence.TelemetryDao
import com.example.advancedtracker.data.local.persistence.toRecord
import com.example.advancedtracker.data.local.persistence.toStoredEntity
import com.example.advancedtracker.data.remote.network.RemoteTelemetryApi
import com.example.advancedtracker.domain.persistence.FailureKind
import com.example.advancedtracker.domain.persistence.RefreshResult
import com.example.advancedtracker.domain.persistence.StoredTelemetryRepository
import com.example.advancedtracker.domain.persistence.TelemetryRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTelemetryRepository @Inject constructor(
    private val api: RemoteTelemetryApi,
    private val dao: TelemetryDao
) : StoredTelemetryRepository {

    override fun observeRecords(): Flow<List<TelemetryRecord>> =
        dao.observeAll().map { entities -> entities.map { it.toRecord() } }

    override suspend fun refresh(): RefreshResult {
        val remote = try {
            api.fetchHistory(limit = 50)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (http: HttpException) {
            return RefreshResult.Failed(
                FailureKind.NETWORK, "La API respondió HTTP ${http.code()}."
            )
        } catch (io: IOException) {
            return RefreshResult.Failed(
                FailureKind.NETWORK, "No fue posible conectar con la API."
            )
        } catch (error: Exception) {
            return RefreshResult.Failed(
                FailureKind.NETWORK, "La respuesta de la API no pudo procesarse."
            )
        }

        return try {
            dao.mergeRemote(remote.map { it.toStoredEntity() })
            RefreshResult.Updated(remote.size)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            RefreshResult.Failed(
                FailureKind.LOCAL_STORAGE,
                "La API respondió, pero no fue posible guardar los datos en Room."
            )
        }
    }
}