package hu.blu3berry.avalon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import hu.blu3berry.avalon.auth.LoginScreen
import hu.blu3berry.avalon.auth.RegisterScreen
import hu.blu3berry.avalon.core.domain.repository.AuthRepository
import hu.blu3berry.avalon.core.domain.session.AuthEvent
import hu.blu3berry.avalon.core.domain.session.SessionManager
import hu.blu3berry.avalon.game.GameScreen
import hu.blu3berry.avalon.home.HomeScreen
import hu.blu3berry.avalon.lobby.LobbyScreen
import hu.blu3berry.avalon.profile.ProfileScreen
import hu.blu3berry.avalon.theme.AvalonTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data class LoginRoute(val sessionExpired: Boolean = false)

@Serializable
data object RegisterRoute

@Serializable
data object HomeRoute

@Serializable
data class LobbyRoute(val lobbyCode: String)

@Serializable
data class GameRoute(val lobbyCode: String)

@Serializable
data object ProfileRoute

@Composable
fun App() {
    AvalonTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding(),
        ) {
            AvalonNavHost()
        }
    }
}

@Composable
private fun AvalonNavHost(
    authRepository: AuthRepository = koinInject(),
    sessionManager: SessionManager = koinInject(),
) {
    // Start destination depends on a stored token; render nothing for the one frame it takes.
    val loggedIn by produceState<Boolean?>(initialValue = null) {
        value = authRepository.isLoggedIn()
    }
    val startLoggedIn = loggedIn ?: return

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // Single exit path back to login: manual logout and expired sessions both land here.
    LaunchedEffect(navController) {
        sessionManager.events.collect { event ->
            val expired = event is AuthEvent.SessionExpired
            navController.navigate(LoginRoute(sessionExpired = expired)) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (startLoggedIn) HomeRoute else LoginRoute(),
    ) {
        composable<LoginRoute> { entry ->
            val route = entry.toRoute<LoginRoute>()
            LoginScreen(
                sessionExpired = route.sessionExpired,
                onLoggedIn = {
                    navController.navigate(HomeRoute) { popUpTo(0) { inclusive = true } }
                },
                onRegisterClick = { navController.navigate(RegisterRoute) },
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegisteredAndLoggedIn = {
                    navController.navigate(HomeRoute) { popUpTo(0) { inclusive = true } }
                },
                onRegistered = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable<HomeRoute> {
            HomeScreen(
                onEnterLobby = { code -> navController.navigate(LobbyRoute(code)) },
                onProfileClick = { navController.navigate(ProfileRoute) },
                onLogout = { scope.launch { authRepository.logout() } },
            )
        }
        composable<LobbyRoute> { entry ->
            val route = entry.toRoute<LobbyRoute>()
            LobbyScreen(
                lobbyCode = route.lobbyCode,
                onGameStarted = {
                    navController.navigate(GameRoute(route.lobbyCode)) {
                        popUpTo<HomeRoute>()
                    }
                },
                onLeft = { navController.popBackStack() },
            )
        }
        composable<ProfileRoute> {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable<GameRoute> { entry ->
            val route = entry.toRoute<GameRoute>()
            GameScreen(
                lobbyCode = route.lobbyCode,
                onExit = {
                    navController.navigate(HomeRoute) { popUpTo(0) { inclusive = true } }
                },
            )
        }
    }
}
