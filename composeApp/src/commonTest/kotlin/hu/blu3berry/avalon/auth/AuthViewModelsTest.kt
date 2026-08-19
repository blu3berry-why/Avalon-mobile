package hu.blu3berry.avalon.auth

import app.cash.turbine.test
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private class FakeAuthRepository : AuthRepository {
    var loginResult: EmptyResult<DataError.Network> = Result.Success(Unit)
    var registerResult: EmptyResult<DataError.Network> = Result.Success(Unit)
    var loginCalls = 0

    override suspend fun login(username: String, password: String): EmptyResult<DataError.Network> {
        loginCalls++
        return loginResult
    }

    override suspend fun register(
        username: String,
        password: String,
        email: String?,
    ): EmptyResult<DataError.Network> = registerResult

    override suspend fun logout() = Unit
    override suspend fun isLoggedIn(): Boolean = false
}

class AuthViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun loginVm(sessionExpired: Boolean = false) =
        LoginViewModel(repository, sessionExpired).apply {
            onAction(LoginAction.UsernameChanged("merlin"))
            onAction(LoginAction.PasswordChanged("excalibur"))
        }

    @Test
    fun `login success emits LoggedIn`() = runTest(dispatcher) {
        val vm = loginVm()
        vm.events.test {
            vm.onAction(LoginAction.Submit)
            assertEquals(LoginEvent.LoggedIn, awaitItem())
        }
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `login failure surfaces error and clears sessionExpired banner`() = runTest(dispatcher) {
        repository.loginResult = Result.Failure(DataError.Network.UNAUTHORIZED)
        val vm = loginVm(sessionExpired = true)
        vm.onAction(LoginAction.Submit)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(DataError.Network.UNAUTHORIZED, vm.state.value.error)
        assertFalse(vm.state.value.sessionExpired)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `blank credentials never submit`() = runTest(dispatcher) {
        val vm = LoginViewModel(repository)
        vm.onAction(LoginAction.Submit)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, repository.loginCalls)
    }

    private fun registerVm() = RegisterViewModel(repository).apply {
        onAction(RegisterAction.UsernameChanged("percival"))
        onAction(RegisterAction.PasswordChanged("grail"))
    }

    @Test
    fun `register success auto-logs-in`() = runTest(dispatcher) {
        val vm = registerVm()
        vm.events.test {
            vm.onAction(RegisterAction.Submit)
            assertEquals(RegisterEvent.RegisteredAndLoggedIn, awaitItem())
        }
        assertEquals(1, repository.loginCalls)
    }

    @Test
    fun `register success with failed login falls back to Registered`() = runTest(dispatcher) {
        repository.loginResult = Result.Failure(DataError.Network.SERVER_ERROR)
        val vm = registerVm()
        vm.events.test {
            vm.onAction(RegisterAction.Submit)
            assertEquals(RegisterEvent.Registered, awaitItem())
        }
    }

    @Test
    fun `register failure surfaces error without login attempt`() = runTest(dispatcher) {
        repository.registerResult = Result.Failure(DataError.Network.CONFLICT)
        val vm = registerVm()
        vm.onAction(RegisterAction.Submit)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(DataError.Network.CONFLICT, vm.state.value.error)
        assertEquals(0, repository.loginCalls)
    }
}
