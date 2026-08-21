package hu.blu3berry.avalon.auth

import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.onFailure
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.mvi.MviViewModel
import kotlinx.coroutines.launch

data class RegisterState(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: DataError.Network? = null,
) {
    val canSubmit: Boolean = username.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface RegisterAction {
    data class UsernameChanged(val value: String) : RegisterAction
    data class PasswordChanged(val value: String) : RegisterAction
    data class EmailChanged(val value: String) : RegisterAction
    data object Submit : RegisterAction
}

sealed interface RegisterEvent {
    /** Registration succeeded and the fresh credentials logged in — go straight to Home. */
    data object RegisteredAndLoggedIn : RegisterEvent
    /** Registration succeeded but the follow-up login failed — fall back to the login screen. */
    data object Registered : RegisterEvent
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<RegisterState, RegisterAction, RegisterEvent>(RegisterState()) {

    override fun onAction(action: RegisterAction) {
        when (action) {
            is RegisterAction.UsernameChanged -> updateState { copy(username = action.value, error = null) }
            is RegisterAction.PasswordChanged -> updateState { copy(password = action.value, error = null) }
            is RegisterAction.EmailChanged -> updateState { copy(email = action.value, error = null) }
            RegisterAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val username = current.username.trim()
            authRepository.register(
                username = username,
                password = current.password,
                email = current.email.trim().ifBlank { null },
            )
                .onSuccess {
                    // Register never returns a token; log in with the same credentials.
                    authRepository.login(username, current.password)
                        .onSuccess {
                            updateState { copy(isLoading = false) }
                            sendEvent(RegisterEvent.RegisteredAndLoggedIn)
                        }
                        .onFailure {
                            updateState { copy(isLoading = false) }
                            sendEvent(RegisterEvent.Registered)
                        }
                }
                .onFailure { error -> updateState { copy(isLoading = false, error = error) } }
        }
    }
}
