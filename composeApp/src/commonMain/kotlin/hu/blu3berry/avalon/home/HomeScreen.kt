package hu.blu3berry.avalon.home

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

// Phase 3 placeholder: lobby create/join lands here.
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text("Home", style = MaterialTheme.typography.headlineLarge)
        Text("Lobby screens arrive in Phase 3.", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onLogout) { Text("Log out") }
    }
}
