package hu.blu3berry.avalon.auth

import app.cash.turbine.test
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private class FakeAuthRepository(
    var loginResult: EmptyResult<DataError.Network> = Result.Success(Unit),
    var registerResult: EmptyResult<DataError.Network> = Result.Success(Unit),
) : AuthRepository {
    val loginCalls = mutableListOf<Pair<String, String>>()
    val registerCalls = mutableListOf<Triple<String, String, String?>>()

    override suspend fun login(username: String, password: String): EmptyResult<DataError.Network> {
        loginCalls += username to password
        return loginResult
    }

    override suspend fun register(
        username: String,
        password: String,
        email: String?,
    ): EmptyResult<DataError.Network> {
        registerCalls += Triple(username, password, email)
        return registerResult
    }

    override suspend fun logout() = Unit

    override suspend fun isLoggedIn(): Boolean = false
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun AuthViewModel.fillCredentials(username: String = "arthur", password: String = "excalibur") {
        onAction(AuthAction.UsernameChanged(username))
        onAction(AuthAction.PasswordChanged(password))
    }

    @Test
    fun `successful login authenticates`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.state.test {
            awaitItem() // initial
            viewModel.fillCredentials()
            skipItems(2)

            viewModel.onAction(AuthAction.Submitted)
            assertTrue(awaitItem().isSubmitting)

            val done = awaitItem()
            assertTrue(done.authenticated)
            assertFalse(done.isSubmitting)
            assertEquals(listOf("arthur" to "excalibur"), repository.loginCalls)
        }
    }

    @Test
    fun `failed login surfaces the error and stays signed out`() = runTest(dispatcher) {
        val repository = FakeAuthRepository(
            loginResult = Result.Failure(DataError.Network.FORBIDDEN),
        )
        val viewModel = AuthViewModel(repository)
        viewModel.fillCredentials()

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(AuthAction.Submitted)
            skipItems(1) // isSubmitting

            val failed = awaitItem()
            assertEquals(DataError.Network.FORBIDDEN, failed.error)
            assertFalse(failed.authenticated)
            assertFalse(failed.isSubmitting)
        }
    }

    @Test
    fun `register returns to the login form without authenticating`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)
        viewModel.onAction(AuthAction.ModeToggled)
        viewModel.fillCredentials()
        viewModel.onAction(AuthAction.EmailChanged("arthur@camelot.example"))

        viewModel.state.test {
            skipItems(1)
            viewModel.onAction(AuthAction.Submitted)
            skipItems(1) // isSubmitting

            val done = awaitItem()
            assertEquals(AuthMode.LOGIN, done.mode)
            assertTrue(done.justRegistered)
            assertFalse(done.authenticated)
            assertEquals(
                Triple<String, String, String?>("arthur", "excalibur", "arthur@camelot.example"),
                repository.registerCalls.single(),
            )
            assertTrue(repository.loginCalls.isEmpty())
        }
    }

    @Test
    fun `blank credentials do not submit`() = runTest(dispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onAction(AuthAction.UsernameChanged("arthur"))
        viewModel.onAction(AuthAction.Submitted)

        assertFalse(viewModel.state.value.isSubmitting)
        assertTrue(repository.loginCalls.isEmpty())
    }
}
