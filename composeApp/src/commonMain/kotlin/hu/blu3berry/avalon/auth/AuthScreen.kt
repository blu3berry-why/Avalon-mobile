package hu.blu3berry.avalon.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.blu3berry.avalon.core.domain.result.DataError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.authenticated) {
        if (state.authenticated) onAuthenticated()
    }

    AuthContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

@Composable
internal fun AuthContent(
    state: AuthState,
    onAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val fieldModifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)

        Text(
            text = if (state.mode == AuthMode.LOGIN) "Sign in" else "Create account",
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = state.username,
            onValueChange = { onAction(AuthAction.UsernameChanged(it)) },
            label = { Text("Username") },
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier = fieldModifier,
        )

        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(AuthAction.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            enabled = !state.isSubmitting,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            modifier = fieldModifier,
        )

        if (state.mode == AuthMode.REGISTER) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(AuthAction.EmailChanged(it)) },
                label = { Text("Email (optional)") },
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = fieldModifier,
            )
        }

        if (state.justRegistered) {
            Text(
                text = "Account created — sign in to continue.",
                color = MaterialTheme.colorScheme.primary,
            )
        }

        state.error?.let { error ->
            Text(
                text = error.message(state.mode),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(
            onClick = { onAction(AuthAction.Submitted) },
            enabled = state.canSubmit,
            modifier = fieldModifier,
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(if (state.mode == AuthMode.LOGIN) "Sign in" else "Register")
        }

        TextButton(
            onClick = { onAction(AuthAction.ModeToggled) },
            enabled = !state.isSubmitting,
        ) {
            Text(
                if (state.mode == AuthMode.LOGIN) "No account? Register"
                else "Already registered? Sign in",
            )
        }
    }
}

/**
 * ForwardAuth answers 403 for a wrong password and 404 for an unknown username
 * (see `forwardauth-api.yaml`); both are shown as one message so the form does not
 * confirm which usernames exist.
 */
internal fun DataError.Network.message(mode: AuthMode): String = when (this) {
    DataError.Network.FORBIDDEN,
    DataError.Network.NOT_FOUND,
    DataError.Network.UNAUTHORIZED,
    -> "Wrong username or password."
    DataError.Network.CONFLICT ->
        if (mode == AuthMode.REGISTER) "That username is taken." else "Could not sign in."
    DataError.Network.NO_INTERNET -> "No connection."
    DataError.Network.REQUEST_TIMEOUT -> "The server took too long to answer."
    DataError.Network.SERVER_ERROR,
    DataError.Network.SERVICE_UNAVAILABLE,
    -> "The server is unavailable. Try again later."
    else -> "Something went wrong. Try again."
}
