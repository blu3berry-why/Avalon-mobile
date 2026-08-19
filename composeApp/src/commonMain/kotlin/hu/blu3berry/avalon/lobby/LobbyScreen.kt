package hu.blu3berry.avalon.lobby

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.blu3berry.avalon.core.domain.model.LobbySettings
import hu.blu3berry.avalon.ui.toUserMessage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LobbyScreen(
    lobbyCode: String,
    onGameStarted: () -> Unit,
    onLeft: () -> Unit,
    viewModel: LobbyViewModel = koinViewModel { parametersOf(lobbyCode) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LobbyEvent.GameStarted -> onGameStarted()
                LobbyEvent.Left -> onLeft()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Lobby", style = MaterialTheme.typography.headlineMedium)
        Text(
            state.lobbyCode,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("Share this code with the other players", style = MaterialTheme.typography.bodySmall)

        Text("Players (${state.players.size})", style = MaterialTheme.typography.titleMedium)
        state.players.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }

        state.settings?.let { settings ->
            Text("Optional roles", style = MaterialTheme.typography.titleMedium)
            SettingsToggles(
                settings = settings,
                onChanged = { viewModel.onAction(LobbyAction.SettingsChanged(it)) },
            )
            OutlinedButton(
                onClick = { viewModel.onAction(LobbyAction.SaveSettings) },
                enabled = !state.isSavingSettings,
                modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
            ) {
                Text(if (state.isSavingSettings) "Saving…" else "Save settings")
            }
        }

        state.error?.let {
            Text(
                it.toUserMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = { viewModel.onAction(LobbyAction.Start) },
            enabled = !state.isStarting,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        ) {
            Text(if (state.isStarting) "Starting…" else "Start game")
        }

        TextButton(onClick = { viewModel.onAction(LobbyAction.Leave) }) {
            Text("Leave lobby")
        }
    }
}

@Composable
private fun SettingsToggles(
    settings: LobbySettings,
    onChanged: (LobbySettings) -> Unit,
) {
    SettingRow("Assassin", settings.assassin) { onChanged(settings.copy(assassin = it)) }
    SettingRow("Mordred", settings.mordred) { onChanged(settings.copy(mordred = it)) }
    SettingRow("Morgana", settings.morgana) { onChanged(settings.copy(morgana = it)) }
    SettingRow("Oberon", settings.oberon) { onChanged(settings.copy(oberon = it)) }
    SettingRow("Percival", settings.percival) { onChanged(settings.copy(percival = it)) }
    SettingRow("Arnold", settings.arnold) { onChanged(settings.copy(arnold = it)) }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
