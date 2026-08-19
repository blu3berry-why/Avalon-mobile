package hu.blu3berry.avalon.game

import androidx.lifecycle.viewModelScope
import hu.blu3berry.avalon.core.domain.model.Character
import hu.blu3berry.avalon.core.domain.model.GameInfo
import hu.blu3berry.avalon.core.domain.model.Score
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.repository.GameRepository
import hu.blu3berry.avalon.core.domain.result.DataError
import hu.blu3berry.avalon.core.domain.result.Result
import hu.blu3berry.avalon.core.domain.result.onFailure
import hu.blu3berry.avalon.core.domain.result.onSuccess
import hu.blu3berry.avalon.mvi.MviViewModel
import kotlinx.coroutines.launch

/** What the screen shows right now, derived from the polled [GameInfo]. */
enum class GamePhase {
    /** No game info received yet. */
    LOADING,
    /** The king is picking the adventure team. */
    TEAM_SELECTION,
    /** A proposed team is up for approval. */
    TEAM_VOTE,
    /** The selected players play success/fail cards. */
    ADVENTURE_VOTE,
    /** Good won three adventures — the assassin names their Merlin guess. */
    ASSASSIN_GUESS,
    /** The game is over. */
    OUTCOME,
}

data class GameState(
    val lobbyCode: String,
    val username: String? = null,
    val info: GameInfo? = null,
    val character: Character? = null,
    val roleVisible: Boolean = false,
    /** Cleared whenever the proposed team changes, so each proposal gets one vote. */
    val teamVoteCast: Boolean = false,
    /** Cleared whenever the adventure number changes. */
    val adventureVoteCast: Boolean = false,
    val merlinGuessCast: Boolean = false,
    val selectedTeam: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val error: DataError.Network? = null,
) {
    val isKing: Boolean get() = username != null && info?.king == username
    val isOnAdventure: Boolean get() = username != null && info?.selectedForAdventure?.contains(username) == true
    val isAssassin: Boolean get() = character?.role == hu.blu3berry.avalon.core.domain.model.Role.ASSASSIN

    val phase: GamePhase
        get() {
            val info = info ?: return GamePhase.LOADING
            return when {
                info.winner != Winner.NOT_DECIDED -> GamePhase.OUTCOME
                info.scores.count { it == Score.GOOD } >= 3 && !info.assassinHasGuessed ->
                    GamePhase.ASSASSIN_GUESS
                info.isAdventure -> GamePhase.ADVENTURE_VOTE
                info.selectedForAdventure.isEmpty() -> GamePhase.TEAM_SELECTION
                else -> GamePhase.TEAM_VOTE
            }
        }
}

sealed interface GameAction {
    data object ToggleRoleVisible : GameAction
    data class ToggleTeamMember(val player: String) : GameAction
    data object SubmitTeam : GameAction
    data class VoteOnTeam(val approve: Boolean) : GameAction
    data class VoteOnAdventure(val succeed: Boolean) : GameAction
    data class GuessMerlin(val player: String) : GameAction
}

class GameViewModel(
    private val gameRepository: GameRepository,
    authRepository: AuthRepository,
    lobbyCode: String,
) : MviViewModel<GameState, GameAction, Nothing>(GameState(lobbyCode)) {

    private val lobbyCode: String get() = state.value.lobbyCode

    init {
        viewModelScope.launch {
            val name = authRepository.currentUsername()
            updateState { copy(username = name) }
        }
        viewModelScope.launch {
            gameRepository.observeGameInfo(lobbyCode).collect { result ->
                when (result) {
                    is Result.Success -> onInfo(result.data)
                    is Result.Failure ->
                        // UNAUTHORIZED terminates the flow and the app is already routing to
                        // login; anything else is a dropped poll — show it, keep collecting.
                        if (result.error != DataError.Network.UNAUTHORIZED) {
                            updateState { copy(error = result.error) }
                        }
                }
            }
        }
    }

    private suspend fun onInfo(info: GameInfo) {
        val previous = state.value.info
        updateState {
            copy(
                info = info,
                error = null,
                // A new proposal or round gets a fresh team vote…
                teamVoteCast = teamVoteCast && previous != null &&
                    previous.selectedForAdventure == info.selectedForAdventure &&
                    previous.currentRound == info.currentRound,
                // …and a new adventure gets fresh success/fail cards.
                adventureVoteCast = adventureVoteCast &&
                    previous?.currentAdventure == info.currentAdventure,
                selectedTeam = if (previous?.currentRound == info.currentRound) selectedTeam else emptySet(),
            )
        }
        // The character is fixed at start; fetch until it lands (early polls can race the
        // server's role assignment), then never again.
        if (state.value.character == null) {
            gameRepository.getCharacter(lobbyCode)
                .onSuccess { character -> updateState { copy(character = character) } }
        }
    }

    override fun onAction(action: GameAction) {
        when (action) {
            GameAction.ToggleRoleVisible -> updateState { copy(roleVisible = !roleVisible) }
            is GameAction.ToggleTeamMember -> updateState {
                copy(
                    selectedTeam = if (action.player in selectedTeam) {
                        selectedTeam - action.player
                    } else {
                        selectedTeam + action.player
                    },
                )
            }
            GameAction.SubmitTeam -> submitTeam()
            is GameAction.VoteOnTeam -> voteOnTeam(action.approve)
            is GameAction.VoteOnAdventure -> voteOnAdventure(action.succeed)
            is GameAction.GuessMerlin -> guessMerlin(action.player)
        }
    }

    private fun submitTeam() {
        val current = state.value
        val required = current.info?.playerSelectNum ?: return
        if (current.selectedTeam.size != required || current.isSubmitting) return
        submit {
            gameRepository.selectForAdventure(lobbyCode, current.selectedTeam.toList())
        }
    }

    private fun voteOnTeam(approve: Boolean) {
        val username = state.value.username ?: return
        if (state.value.teamVoteCast || state.value.isSubmitting) return
        submit(onSuccess = { copy(teamVoteCast = true) }) {
            gameRepository.voteOnTeam(lobbyCode, username, approve)
        }
    }

    private fun voteOnAdventure(succeed: Boolean) {
        val username = state.value.username ?: return
        if (state.value.adventureVoteCast || state.value.isSubmitting) return
        submit(onSuccess = { copy(adventureVoteCast = true) }) {
            gameRepository.voteOnAdventure(lobbyCode, username, succeed)
        }
    }

    private fun guessMerlin(player: String) {
        if (state.value.merlinGuessCast || state.value.isSubmitting) return
        submit(onSuccess = { copy(merlinGuessCast = true) }) {
            gameRepository.guessMerlin(lobbyCode, player)
        }
    }

    private fun submit(
        onSuccess: GameState.() -> GameState = { this },
        call: suspend () -> Result<Unit, DataError.Network>,
    ) {
        updateState { copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            call()
                .onSuccess { updateState { copy(isSubmitting = false).onSuccess() } }
                .onFailure { error -> updateState { copy(isSubmitting = false, error = error) } }
        }
    }
}
