package hu.blu3berry.avalon.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.blu3berry.avalon.core.domain.model.Score
import hu.blu3berry.avalon.core.domain.model.Winner
import hu.blu3berry.avalon.ui.toUserMessage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameScreen(
    lobbyCode: String,
    onExit: () -> Unit,
    viewModel: GameViewModel = koinViewModel { parametersOf(lobbyCode) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Round ${state.info?.currentRound ?: "–"}", style = MaterialTheme.typography.titleMedium)
        state.info?.let { ScoreRow(it.scores) }

        RoleSection(state, onToggle = { viewModel.onAction(GameAction.ToggleRoleVisible) })

        when (state.phase) {
            GamePhase.LOADING -> CircularProgressIndicator()
            GamePhase.TEAM_SELECTION -> TeamSelection(state, viewModel::onAction)
            GamePhase.TEAM_VOTE -> TeamVote(state, viewModel::onAction)
            GamePhase.ADVENTURE_VOTE -> AdventureVote(state, viewModel::onAction)
            GamePhase.ASSASSIN_GUESS -> AssassinGuess(state, viewModel::onAction)
            GamePhase.OUTCOME -> Outcome(state, onExit)
        }

        state.error?.let {
            Text(
                it.toUserMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ScoreRow(scores: List<Score>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        scores.forEach { score ->
            Text(
                when (score) {
                    Score.GOOD -> "🔵"
                    Score.EVIL -> "🔴"
                    Score.UNDECIDED -> "⚪"
                },
            )
        }
    }
}

@Composable
private fun RoleSection(state: GameState, onToggle: () -> Unit) {
    val character = state.character ?: return
    TextButton(onClick = onToggle) {
        Text(if (state.roleVisible) "Hide role" else "Show role")
    }
    if (state.roleVisible) {
        Text(
            character.role.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.headlineSmall,
            color = if (character.role.isEvil) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        if (character.sees.isNotEmpty()) {
            Text("You see: ${character.sees.joinToString()}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TeamSelection(state: GameState, onAction: (GameAction) -> Unit) {
    val info = state.info ?: return
    if (state.isKing) {
        Text(
            "You are the king — pick ${info.playerSelectNum} players",
            style = MaterialTheme.typography.titleMedium,
        )
        info.players.forEach { player ->
            Row(
                modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(player)
                Checkbox(
                    checked = player in state.selectedTeam,
                    onCheckedChange = { onAction(GameAction.ToggleTeamMember(player)) },
                )
            }
        }
        Button(
            onClick = { onAction(GameAction.SubmitTeam) },
            enabled = state.selectedTeam.size == info.playerSelectNum && !state.isSubmitting,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        ) {
            Text("Propose team (${state.selectedTeam.size}/${info.playerSelectNum})")
        }
    } else {
        Text(
            "King ${info.king ?: "?"} is picking the team…",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun TeamVote(state: GameState, onAction: (GameAction) -> Unit) {
    val info = state.info ?: return
    Text("Proposed team", style = MaterialTheme.typography.titleMedium)
    info.selectedForAdventure.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
    if (state.teamVoteCast) {
        Text("Vote cast — waiting for the others…", style = MaterialTheme.typography.bodyMedium)
    } else {
        VoteButtons(
            first = "Approve",
            second = "Reject",
            enabled = !state.isSubmitting,
            onFirst = { onAction(GameAction.VoteOnTeam(approve = true)) },
            onSecond = { onAction(GameAction.VoteOnTeam(approve = false)) },
        )
    }
    Text("Failed proposals this round: ${info.failCounter}", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun AdventureVote(state: GameState, onAction: (GameAction) -> Unit) {
    if (!state.isOnAdventure) {
        Text("The adventure is underway…", style = MaterialTheme.typography.titleMedium)
        return
    }
    if (state.adventureVoteCast) {
        Text("Card played — waiting for the others…", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text("Play your card", style = MaterialTheme.typography.titleMedium)
    VoteButtons(
        first = "Succeed",
        second = "Fail",
        enabled = !state.isSubmitting,
        // Good must play success — the classic rule; the server rejects it anyway.
        secondEnabled = state.character?.role?.isEvil == true,
        onFirst = { onAction(GameAction.VoteOnAdventure(succeed = true)) },
        onSecond = { onAction(GameAction.VoteOnAdventure(succeed = false)) },
    )
}

@Composable
private fun AssassinGuess(state: GameState, onAction: (GameAction) -> Unit) {
    val info = state.info ?: return
    if (!state.isAssassin) {
        Text("Good found the Grail… but the assassin aims for Merlin.", style = MaterialTheme.typography.titleMedium)
        return
    }
    if (state.merlinGuessCast) {
        Text("Guess made…", style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text("Who is Merlin?", style = MaterialTheme.typography.titleMedium)
    val candidates = state.character?.let { c -> info.players - c.sees.toSet() - setOfNotNull(state.username) }
        ?: info.players
    candidates.forEach { player ->
        OutlinedButton(
            onClick = { onAction(GameAction.GuessMerlin(player)) },
            enabled = !state.isSubmitting,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        ) {
            Text(player)
        }
    }
}

@Composable
private fun Outcome(state: GameState, onExit: () -> Unit) {
    val info = state.info ?: return
    Text(
        when (info.winner) {
            Winner.GOOD -> "Good triumphs! 🔵"
            Winner.EVIL -> "Evil prevails! 🔴"
            Winner.NOT_DECIDED -> ""
        },
        style = MaterialTheme.typography.headlineMedium,
    )
    Button(onClick = onExit, modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth()) {
        Text("Back to home")
    }
}

@Composable
private fun VoteButtons(
    first: String,
    second: String,
    enabled: Boolean,
    onFirst: () -> Unit,
    onSecond: () -> Unit,
    secondEnabled: Boolean = true,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onFirst, enabled = enabled) { Text(first) }
        OutlinedButton(onClick = onSecond, enabled = enabled && secondEnabled) { Text(second) }
    }
}
