package hu.blu3berry.avalon.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Phase 4 placeholder: the observeGameInfo-driven game screen lands here.
@Composable
fun GameScreen(lobbyCode: String, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text("Game $lobbyCode", style = MaterialTheme.typography.headlineLarge)
        Text("The game screen arrives in Phase 4.", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onExit) { Text("Back to home") }
    }
}
