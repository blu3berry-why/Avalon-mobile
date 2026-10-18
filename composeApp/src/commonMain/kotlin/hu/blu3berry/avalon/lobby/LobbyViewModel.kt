package hu.blu3berry.avalon.lobby

import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.core.domain.repository.GameRepository
import hu.blu3berry.avalon.core.domain.repository.LobbyRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.result.onFailure
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.mvi.MviViewModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LobbyState(
    val lobbyCode: String,
    val players: List<String> = emptyList(),
    /** null until the first successful settings fetch. */
    val settings: LobbySettings? = null,
    val isSavingSettings: Boolean = false,
    val isStarting: Boolean = false,
    val error: DataError.Network? = null,
)

sealed interface LobbyAction {
    data class SettingsChanged(val settings: LobbySettings) : LobbyAction
    data object SaveSettings : LobbyAction
    data object Start : LobbyAction
    data object Leave : LobbyAction
}

sealed interface LobbyEvent {
    data object Left : LobbyEvent
    data object GameStarted : LobbyEvent
}

/**
 * @param pollInterval lobby refresh cadence — injectable so tests use virtual time.
 */
class LobbyViewModel(
    private val lobbyRepository: LobbyRepository,
    private val gameRepository: GameRepository,
    lobbyCode: String,
    private val pollInterval: Duration = 2.seconds,
) : MviViewModel<LobbyState, LobbyAction, LobbyEvent>(LobbyState(lobbyCode)) {

    private val lobbyCode: String get() = state.value.lobbyCode

    init {
        viewModelScope.launch {
            lobbyRepository.getSettings(lobbyCode)
                .onSuccess { settings -> updateState { copy(settings = settings) } }
        }
        viewModelScope.launch { pollLoop() }
    }

    /**
     * One loop drives the whole screen: refresh the player list, and probe the game state so
     * everyone (not only the player who pressed Start) moves on when the game begins. The
     * probe's failures are ignored — the server may not have game info before start.
     * A 401 kills the loop; the app is routing back to login by then anyway.
     */
    private suspend fun pollLoop() {
        while (true) {
            val names = lobbyRepository.getPlayerNames(lobbyCode)
            when (names) {
                is Result.Success -> updateState { copy(players = names.data) }
                is Result.Failure -> {
                    if (names.error == DataError.Network.UNAUTHORIZED) return
                    updateState { copy(error = names.error) }
                }
            }

            val info = gameRepository.getGameInfo(lobbyCode)
            if (info is Result.Success && info.data.started) {
                sendEvent(LobbyEvent.GameStarted)
                return
            }

            delay(pollInterval)
        }
    }

    override fun onAction(action: LobbyAction) {
        when (action) {
            is LobbyAction.SettingsChanged -> updateState { copy(settings = action.settings) }
            LobbyAction.SaveSettings -> saveSettings()
            LobbyAction.Start -> start()
            LobbyAction.Leave -> leave()
        }
    }

    private fun saveSettings() {
        val settings = state.value.settings ?: return
        updateState { copy(isSavingSettings = true, error = null) }
        viewModelScope.launch {
            lobbyRepository.updateSettings(lobbyCode, settings)
                .onSuccess { updateState { copy(isSavingSettings = false) } }
                .onFailure { error ->
                    updateState { copy(isSavingSettings = false, error = error) }
                }
        }
    }

    private fun start() {
        if (state.value.isStarting) return
        updateState { copy(isStarting = true, error = null) }
        viewModelScope.launch {
            lobbyRepository.start(lobbyCode)
                .onSuccess {
                    updateState { copy(isStarting = false) }
                    sendEvent(LobbyEvent.GameStarted)
                }
                .onFailure { error -> updateState { copy(isStarting = false, error = error) } }
        }
    }

    private fun leave() {
        viewModelScope.launch {
            // Navigate away regardless: a failed leave (dead lobby, lost connection) must not
            // trap the player on the screen.
            lobbyRepository.leave(lobbyCode)
            sendEvent(LobbyEvent.Left)
        }
    }
}
