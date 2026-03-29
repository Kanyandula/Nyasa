package com.kanyandula.nyasa.ui.main.account.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    isLoading: Boolean,
    onUpdatePassword: (current: String, new: String, confirmNew: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }

    val passwordMismatch = confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword
    val canSubmit = currentPassword.isNotEmpty() &&
        newPassword.isNotEmpty() &&
        confirmNewPassword.isNotEmpty() &&
        !passwordMismatch

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Change Password",
                    onNavigationClick = onNavigateBack
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Secure Your Account",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ensure your new password is strong and contains at least 8 characters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                NyasaTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Current Password",
                    leadingIcon = Icons.Filled.Password,
                    isPassword = true,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New Password",
                    leadingIcon = Icons.Filled.VpnKey,
                    isPassword = true,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = "Confirm New Password",
                    leadingIcon = Icons.Filled.VerifiedUser,
                    isPassword = true,
                    isError = passwordMismatch,
                    errorMessage = if (passwordMismatch) "Passwords do not match" else null,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        if (canSubmit) {
                            onUpdatePassword(currentPassword, newPassword, confirmNewPassword)
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "You will be logged out of other devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(32.dp))

                NyasaButton(
                    text = "Update Password",
                    onClick = { onUpdatePassword(currentPassword, newPassword, confirmNewPassword) },
                    enabled = canSubmit,
                    loading = isLoading
                )
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ChangePasswordScreenPreview() {
    NyasaTheme {
        ChangePasswordScreen(
            isLoading = false,
            onUpdatePassword = { _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
