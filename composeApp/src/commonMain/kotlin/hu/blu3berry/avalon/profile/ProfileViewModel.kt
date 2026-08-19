package hu.blu3berry.avalon.profile

import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.model.User
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.repository.UserRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.onFailure
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.mvi.MviViewModel
import kotlinx.coroutines.launch

data class ProfileState(
    val user: User? = null,
    val email: String = "",
    /** Blank means "leave unchanged" — the server treats absent fields that way too. */
    val newPassword: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: DataError.Network? = null,
)

sealed interface ProfileAction {
    data class EmailChanged(val value: String) : ProfileAction
    data class NewPasswordChanged(val value: String) : ProfileAction
    data object Save : ProfileAction
    data object Logout : ProfileAction
    data object DeleteAccount : ProfileAction
}

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : MviViewModel<ProfileState, ProfileAction, Nothing>(ProfileState(isLoading = true)) {

    init {
        viewModelScope.launch {
            val username = authRepository.currentUsername()
            if (username == null) {
                // No stored identity (e.g. pre-Phase-4 login) — logout routes back to auth.
                authRepository.logout()
                return@launch
            }
            userRepository.get(username)
                .onSuccess { user ->
                    updateState {
                        copy(user = user, email = user.email.orEmpty(), isLoading = false)
                    }
                }
                .onFailure { error -> updateState { copy(isLoading = false, error = error) } }
        }
    }

    override fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.EmailChanged ->
                updateState { copy(email = action.value, saved = false, error = null) }
            is ProfileAction.NewPasswordChanged ->
                updateState { copy(newPassword = action.value, saved = false, error = null) }
            ProfileAction.Save -> save()
            ProfileAction.Logout -> viewModelScope.launch { authRepository.logout() }
            ProfileAction.DeleteAccount -> delete()
        }
    }

    private fun save() {
        val current = state.value
        val user = current.user ?: return
        if (current.isSaving) return
        updateState { copy(isSaving = true, saved = false, error = null) }
        viewModelScope.launch {
            userRepository.update(
                username = user.username,
                password = current.newPassword.ifBlank { null },
                email = current.email.trim().ifBlank { null },
            )
                .onSuccess { updated ->
                    updateState {
                        copy(
                            user = updated,
                            email = updated.email.orEmpty(),
                            newPassword = "",
                            isSaving = false,
                            saved = true,
                        )
                    }
                }
                .onFailure { error -> updateState { copy(isSaving = false, error = error) } }
        }
    }

    private fun delete() {
        if (state.value.isSaving) return
        updateState { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            userRepository.delete()
                .onSuccess {
                    // Account is gone; drop the token. LogoutRequired routes to login.
                    authRepository.logout()
                }
                .onFailure { error -> updateState { copy(isSaving = false, error = error) } }
        }
    }
}
