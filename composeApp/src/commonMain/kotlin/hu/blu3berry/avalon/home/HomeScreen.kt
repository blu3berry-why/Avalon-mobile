package hu.blu3berry.avalon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Placeholder for the signed-in area — lobby list and game screens replace it in the next slice.
 * It exists so the auth flow has somewhere to land and a way back out.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authRepository: AuthRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Signed in ✓", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { scope.launch { authRepository.logout() } }) {
            Text("Sign out")
        }
    }
}
