package hu.blu3berry.avalon.home

import app.cash.turbine.test
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.lobby.FakeLobbyRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeLobbyRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `create success enters the new lobby`() = runTest(dispatcher) {
        repository.createResult = Result.Success("WXYZ")
        val vm = HomeViewModel(repository)
        vm.events.test {
            vm.onAction(HomeAction.CreateLobby)
            assertEquals(HomeEvent.EnterLobby("WXYZ"), awaitItem())
        }
    }

    @Test
    fun `join success enters the lobby with the trimmed code`() = runTest(dispatcher) {
        val vm = HomeViewModel(repository)
        vm.onAction(HomeAction.JoinCodeChanged(" ABCD "))
        vm.events.test {
            vm.onAction(HomeAction.JoinLobby)
            assertEquals(HomeEvent.EnterLobby("ABCD"), awaitItem())
        }
    }

    @Test
    fun `join failure surfaces the error`() = runTest(dispatcher) {
        repository.joinResult = Result.Failure(DataError.Network.NOT_FOUND)
        val vm = HomeViewModel(repository)
        vm.onAction(HomeAction.JoinCodeChanged("NOPE"))
        vm.onAction(HomeAction.JoinLobby)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(DataError.Network.NOT_FOUND, vm.state.value.error)
    }
}
