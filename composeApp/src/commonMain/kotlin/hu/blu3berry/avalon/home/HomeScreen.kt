package hu.blu3berry.avalon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.blu3berry.avalon.ui.toUserMessage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onEnterLobby: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.EnterLobby -> onEnterLobby(event.lobbyCode)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text("Avalon", style = MaterialTheme.typography.headlineLarge)

        Button(
            onClick = { viewModel.onAction(HomeAction.CreateLobby) },
            enabled = state.canCreate,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        ) {
            Text("Create lobby")
        }

        HorizontalDivider(modifier = Modifier.widthIn(max = 360.dp))

        OutlinedTextField(
            value = state.joinCode,
            onValueChange = { viewModel.onAction(HomeAction.JoinCodeChanged(it)) },
            label = { Text("Lobby code") },
            singleLine = true,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        )
        OutlinedButton(
            onClick = { viewModel.onAction(HomeAction.JoinLobby) },
            enabled = state.canJoin,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        ) {
            Text("Join lobby")
        }

        state.error?.let {
            Text(
                it.toUserMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        TextButton(onClick = onProfileClick) { Text("Profile") }
        TextButton(onClick = onLogout) { Text("Log out") }
    }
}
