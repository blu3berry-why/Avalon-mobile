package hu.blu3berry.avalon.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode { LOGIN, REGISTER }

data class AuthState(
    val mode: AuthMode = AuthMode.LOGIN,
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val isSubmitting: Boolean = false,
    val error: DataError.Network? = null,
    /** Set after a successful registration; cleared on the next input or submit. */
    val justRegistered: Boolean = false,
    /** The token is stored — the host swaps to the logged-in UI. */
    val authenticated: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isSubmitting && username.isNotBlank() && password.isNotBlank()
}

sealed interface AuthAction {
    data class UsernameChanged(val value: String) : AuthAction
    data class PasswordChanged(val value: String) : AuthAction
    data class EmailChanged(val value: String) : AuthAction
    data object ModeToggled : AuthAction
    data object Submitted : AuthAction
}

class AuthViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.UsernameChanged -> _state.update { it.copy(username = action.value, error = null) }
            is AuthAction.PasswordChanged -> _state.update { it.copy(password = action.value, error = null) }
            is AuthAction.EmailChanged -> _state.update { it.copy(email = action.value, error = null) }
            AuthAction.ModeToggled -> _state.update {
                it.copy(
                    mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                    error = null,
                    justRegistered = false,
                )
            }
            AuthAction.Submitted -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null, justRegistered = false) }

        viewModelScope.launch {
            val result = when (current.mode) {
                AuthMode.LOGIN -> authRepository.login(current.username, current.password)
                AuthMode.REGISTER -> authRepository.register(
                    username = current.username,
                    password = current.password,
                    email = current.email.takeIf { it.isNotBlank() },
                )
            }
            _state.update {
                when (result) {
                    // Register issues no token (see forwardauth-api.yaml), so a new account
                    // lands back on the login form with its credentials still filled in.
                    is Result.Success -> it.copy(
                        isSubmitting = false,
                        authenticated = current.mode == AuthMode.LOGIN,
                        mode = AuthMode.LOGIN,
                        justRegistered = current.mode == AuthMode.REGISTER,
                    )
                    is Result.Failure -> it.copy(isSubmitting = false, error = result.error)
                }
            }
        }
    }
}
