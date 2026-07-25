package hu.blu3berry.avalon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import hu.blu3berry.avalon.auth.AuthScreen
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.session.SessionManager
import hu.blu3berry.avalon.home.HomeScreen
import org.koin.compose.koinInject

@Composable
fun App(
    authRepository: AuthRepository = koinInject(),
    sessionManager: SessionManager = koinInject(),
) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            // A stored token only means "not signed out"; an expired one surfaces as the first
            // 401, which SessionManager turns into the event collected below.
            val startedSignedIn by produceState<Boolean?>(initialValue = null) {
                value = authRepository.isLoggedIn()
            }
            var signedIn by remember(startedSignedIn) { mutableStateOf(startedSignedIn) }

            // Logout and session expiry both land here, so signing out from anywhere in the
            // signed-in area routes back to the form without threading a callback through it.
            LaunchedEffect(Unit) {
                sessionManager.events.collect { signedIn = false }
            }

            // ponytail: two destinations, so a boolean beats a nav graph. Swap for NavDisplay
            // (the nav3 bundle is already in the catalog) when the lobby screens land.
            when (signedIn) {
                null -> Unit // token read is a one-shot disk hit; no spinner worth showing
                true -> HomeScreen()
                false -> AuthScreen(onAuthenticated = { signedIn = true })
            }
        }
    }
}
