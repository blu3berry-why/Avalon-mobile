package hu.blu3berry.avalon.auth

import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.onFailure
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.mvi.MviViewModel
import kotlinx.coroutines.launch

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: DataError.Network? = null,
    /** One-time banner shown when the backend rejected the previous session's token. */
    val sessionExpired: Boolean = false,
) {
    val canSubmit: Boolean = username.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object Submit : LoginAction
}

sealed interface LoginEvent {
    data object LoggedIn : LoginEvent
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    sessionExpired: Boolean = false,
) : MviViewModel<LoginState, LoginAction, LoginEvent>(LoginState(sessionExpired = sessionExpired)) {

    override fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.UsernameChanged -> updateState { copy(username = action.value, error = null) }
            is LoginAction.PasswordChanged -> updateState { copy(password = action.value, error = null) }
            LoginAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        updateState { copy(isLoading = true, error = null, sessionExpired = false) }
        viewModelScope.launch {
            authRepository.login(current.username.trim(), current.password)
                .onSuccess {
                    updateState { copy(isLoading = false) }
                    sendEvent(LoginEvent.LoggedIn)
                }
                .onFailure { error -> updateState { copy(isLoading = false, error = error) } }
        }
    }
}
