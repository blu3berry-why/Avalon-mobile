package hu.blu3berry.avalon.home

import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.repository.LobbyRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.onFailure
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.mvi.MviViewModel
import kotlinx.coroutines.launch

data class HomeState(
    val joinCode: String = "",
    val isLoading: Boolean = false,
    val error: DataError.Network? = null,
) {
    val canJoin: Boolean = joinCode.isNotBlank() && !isLoading
    val canCreate: Boolean = !isLoading
}

sealed interface HomeAction {
    data class JoinCodeChanged(val value: String) : HomeAction
    data object CreateLobby : HomeAction
    data object JoinLobby : HomeAction
}

sealed interface HomeEvent {
    data class EnterLobby(val lobbyCode: String) : HomeEvent
}

class HomeViewModel(
    private val lobbyRepository: LobbyRepository,
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {

    override fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.JoinCodeChanged -> updateState { copy(joinCode = action.value, error = null) }
            HomeAction.CreateLobby -> create()
            HomeAction.JoinLobby -> join()
        }
    }

    private fun create() {
        if (!state.value.canCreate) return
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            lobbyRepository.create()
                .onSuccess { code ->
                    updateState { copy(isLoading = false) }
                    sendEvent(HomeEvent.EnterLobby(code))
                }
                .onFailure { error -> updateState { copy(isLoading = false, error = error) } }
        }
    }

    private fun join() {
        val code = state.value.joinCode.trim()
        if (!state.value.canJoin) return
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            lobbyRepository.join(code)
                .onSuccess {
                    updateState { copy(isLoading = false) }
                    sendEvent(HomeEvent.EnterLobby(code))
                }
                .onFailure { error -> updateState { copy(isLoading = false, error = error) } }
        }
    }
}
