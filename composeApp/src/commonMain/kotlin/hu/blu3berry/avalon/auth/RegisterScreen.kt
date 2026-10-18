package hu.blu3berry.avalon.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onRegisteredAndLoggedIn: () -> Unit,
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.RegisteredAndLoggedIn -> onRegisteredAndLoggedIn()
                RegisterEvent.Registered -> onRegistered()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineLarge)

        OutlinedTextField(
            value = state.username,
            onValueChange = { viewModel.onAction(RegisterAction.UsernameChanged(it)) },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onAction(RegisterAction.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onAction(RegisterAction.EmailChanged(it)) },
            label = { Text("Email (optional)") },
            singleLine = true,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        )

        state.error?.let {
            Text(
                it.toAuthMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = { viewModel.onAction(RegisterAction.Submit) },
            enabled = state.canSubmit,
            modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Register")
            }
        }

        TextButton(onClick = onBackToLogin) {
            Text("Back to login")
        }
    }
}
