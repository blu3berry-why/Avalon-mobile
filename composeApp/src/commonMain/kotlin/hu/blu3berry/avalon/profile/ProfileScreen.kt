package hu.blu3berry.avalon.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.blu3berry.avalon.ui.toUserMessage
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge)

        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            state.user?.let { user ->
                Text(user.username, style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onAction(ProfileAction.EmailChanged(it)) },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.newPassword,
                    onValueChange = { viewModel.onAction(ProfileAction.NewPasswordChanged(it)) },
                    label = { Text("New password (leave blank to keep)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                )
                Button(
                    onClick = { viewModel.onAction(ProfileAction.Save) },
                    enabled = !state.isSaving,
                    modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
                ) {
                    Text(if (state.isSaving) "Saving…" else "Save changes")
                }
                if (state.saved) {
                    Text("Saved ✓", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        state.error?.let {
            Text(
                it.toUserMessage(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        TextButton(onClick = { viewModel.onAction(ProfileAction.Logout) }) { Text("Log out") }
        TextButton(onClick = { confirmDelete = true }) {
            Text("Delete account", color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = onBack) { Text("Back") }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete account?") },
            text = { Text("This permanently removes your account. There is no undo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.onAction(ProfileAction.DeleteAccount)
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }
}
