package hu.blu3berry.avalon.profile

import hu.blu3berry.avalon.core.domain.model.User
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.repository.UserRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.EmptyResult
import hu.blu3berry.avalon.core.domain.result.Result
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

private val arthur = User(username = "arthur", email = "a@camelot.example", friends = emptyList())

private class FakeUserRepository : UserRepository {
    var getResult: Result<User, DataError.Network> = Result.Success(arthur)
    var updateResult: Result<User, DataError.Network> = Result.Success(arthur)
    var deleteResult: Result<User, DataError.Network> = Result.Success(arthur)
    var updateArgs: Triple<String, String?, String?>? = null
    var deleteCalls = 0

    override suspend fun get(username: String) = getResult

    override suspend fun update(username: String, password: String?, email: String?): Result<User, DataError.Network> {
        updateArgs = Triple(username, password, email)
        return updateResult
    }

    override suspend fun delete(): Result<User, DataError.Network> {
        deleteCalls++
        return deleteResult
    }
}

private class FakeAuthRepository : AuthRepository {
    var loggedOut = false
    override suspend fun login(username: String, password: String): EmptyResult<DataError.Network> =
        Result.Success(Unit)
    override suspend fun register(username: String, password: String, email: String?): EmptyResult<DataError.Network> =
        Result.Success(Unit)
    override suspend fun logout() { loggedOut = true }
    override suspend fun isLoggedIn(): Boolean = !loggedOut
    override suspend fun currentUsername(): String? = "arthur"
}

class ProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val userRepository = FakeUserRepository()
    private val authRepository = FakeAuthRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun vm() = ProfileViewModel(userRepository, authRepository)
        .also { dispatcher.scheduler.advanceUntilIdle() }

    @Test
    fun `loads the current user`() = runTest(dispatcher) {
        val vm = vm()
        assertEquals(arthur, vm.state.value.user)
        assertEquals("a@camelot.example", vm.state.value.email)
    }

    @Test
    fun `save sends trimmed email and blank password as unchanged`() = runTest(dispatcher) {
        val vm = vm()
        vm.onAction(ProfileAction.EmailChanged(" new@camelot.example "))
        vm.onAction(ProfileAction.Save)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(Triple("arthur", null, "new@camelot.example"), userRepository.updateArgs)
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun `save failure surfaces the error`() = runTest(dispatcher) {
        userRepository.updateResult = Result.Failure(DataError.Network.CONFLICT)
        val vm = vm()
        vm.onAction(ProfileAction.Save)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(DataError.Network.CONFLICT, vm.state.value.error)
    }

    @Test
    fun `delete logs out on success`() = runTest(dispatcher) {
        val vm = vm()
        vm.onAction(ProfileAction.DeleteAccount)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, userRepository.deleteCalls)
        assertTrue(authRepository.loggedOut)
    }

    @Test
    fun `delete failure keeps the account and shows the error`() = runTest(dispatcher) {
        userRepository.deleteResult = Result.Failure(DataError.Network.SERVER_ERROR)
        val vm = vm()
        vm.onAction(ProfileAction.DeleteAccount)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, authRepository.loggedOut)
        assertEquals(DataError.Network.SERVER_ERROR, vm.state.value.error)
    }
}
