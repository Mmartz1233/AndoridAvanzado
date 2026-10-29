package com.example.advancedtracker.ui.remote



import com.example.advancedtracker.domain.model.tracking.TelemetryData
import com.example.advancedtracker.domain.remote.RemoteErrorKind
import com.example.advancedtracker.domain.remote.RemoteResult
import com.example.advancedtracker.domain.repository.RemoteTelemetryRepository
import com.example.advancedtracker.domain.usecase.remote.FetchRemoteTelemetryUseCase
import com.example.advancedtracker.domain.usecase.remote.SendRemoteTelemetryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteTelemetryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val item = TelemetryData(id = "tel_01", latitude = 19.4326, longitude = -99.1332,
        speedKmh = 0.0, timestamp = Date(1700000000000L), isMocked = true)

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class FakeRepository(item: TelemetryData) : RemoteTelemetryRepository {
        var getCalls = 0
        var postCalls = 0
        var getResult: RemoteResult<List<TelemetryData>> = RemoteResult.Success(listOf(item))
        var postResult: RemoteResult<TelemetryData> = RemoteResult.Success(item)

        override suspend fun fetchHistory(): RemoteResult<List<TelemetryData>> {
            getCalls++
            delay(1)
            return getResult
        }
        override suspend fun sendRecord(data: TelemetryData): RemoteResult<TelemetryData> {
            postCalls++
            delay(1)
            return postResult
        }
    }

    private fun viewModel(fake: FakeRepository) = RemoteTelemetryViewModel(
        FetchRemoteTelemetryUseCase(fake), SendRemoteTelemetryUseCase(fake)
    )

    @Test fun initialLoadReachesSuccess() = runTest {
        val fake = FakeRepository(item)
        val vm = viewModel(fake)
        assertEquals(RemoteTelemetryUiState.Loading, vm.state.value)
        advanceUntilIdle()
        val success = vm.state.value as RemoteTelemetryUiState.Success
        assertEquals(item.id, success.records.first().id)
        assertEquals(1, fake.getCalls)
    }

    @Test fun failedPostDoesNotTriggerGet() = runTest {
        val fake = FakeRepository(item)
        val vm = viewModel(fake)
        advanceUntilIdle()
        fake.postResult = RemoteResult.Error(RemoteErrorKind.HTTP, "Servidor falló", 500)
        vm.sendDemo()
        runCurrent()
        assertEquals(RemoteTelemetryUiState.Loading, vm.state.value)
        advanceUntilIdle()
        assertTrue(vm.state.value is RemoteTelemetryUiState.Error)
        assertEquals(1, fake.postCalls)
        assertEquals(1, fake.getCalls)
    }

    @Test fun successfulPostRefreshesHistory() = runTest {
        val fake = FakeRepository(item)
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.sendDemo()
        advanceUntilIdle()
        val success = vm.state.value as RemoteTelemetryUiState.Success
        assertTrue(success.notice!!.contains(item.id))
        assertEquals(1, fake.postCalls)
        assertEquals(2, fake.getCalls)
    }

    @Test fun failedRefreshAfterPostReportsConfirmedSend() = runTest {
        val fake = FakeRepository(item)
        val vm = viewModel(fake)
        advanceUntilIdle()
        fake.getResult = RemoteResult.Error(RemoteErrorKind.CONNECTION, "Sin red")
        vm.sendDemo()
        advanceUntilIdle()
        val error = vm.state.value as RemoteTelemetryUiState.Error
        assertTrue(error.message.contains("El registro se envió"))
        assertEquals(1, fake.postCalls)
        assertEquals(2, fake.getCalls)
    }

    @Test fun repeatedActionsWhileBusyDoNotStartAnotherOperation() = runTest {
        val fake = FakeRepository(item)
        val vm = viewModel(fake)
        vm.refresh()
        vm.sendDemo()
        advanceUntilIdle()
        assertEquals(1, fake.getCalls)
        assertEquals(0, fake.postCalls)
    }
}