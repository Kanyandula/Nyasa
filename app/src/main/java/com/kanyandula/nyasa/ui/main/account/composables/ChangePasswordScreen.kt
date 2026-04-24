package com.kanyandula.nyasa.ui.main.account.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.analytics.TrackScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    isLoading: Boolean,
    onUpdatePassword: (current: String, new: String, confirmNew: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    TrackScreen("ChangePassword")
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }

    val passwordMismatch = confirmNewPassword.isNotEmpty() &&
        newPassword != confirmNewPassword
    val hasMinLength = newPassword.length >= 8
    val hasNumber = newPassword.any { it.isDigit() }
    val canSubmit = currentPassword.isNotEmpty() &&
        newPassword.isNotEmpty() &&
        confirmNewPassword.isNotEmpty() &&
        !passwordMismatch &&
        hasMinLength

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
                    .padding(horizontal = NyasaTheme.spacing.xl)
            ) {
                Spacer(Modifier.height(NyasaTheme.spacing.m))

                // Lock icon in circle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Secure Your Account",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                Text(
                    text = "Ensure your new password is strong and" +
                        " contains at least 8 characters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))

                // Current Password
                Text(
                    text = "Current Password",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                NyasaTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Enter existing password",
                    leadingIcon = Icons.Filled.LockReset,
                    isPassword = true,
                    imeAction = ImeAction.Next
                )

                Spacer(Modifier.height(20.dp))

                // New Password
                Text(
                    text = "New Password",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                NyasaTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Min. 8 characters",
                    leadingIcon = Icons.Filled.VpnKey,
                    isPassword = true,
                    imeAction = ImeAction.Next
                )

                Spacer(Modifier.height(20.dp))

                // Confirm New Password
                Text(
                    text = "Confirm New Password",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                NyasaTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = "Re-type new password",
                    leadingIcon = Icons.Filled.VerifiedUser,
                    isPassword = true,
                    isError = passwordMismatch,
                    errorMessage = if (passwordMismatch) {
                        "Passwords do not match"
                    } else {
                        null
                    },
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        if (canSubmit) {
                            onUpdatePassword(
                                currentPassword,
                                newPassword,
                                confirmNewPassword
                            )
                        }
                    }
                )

                Spacer(Modifier.height(NyasaTheme.spacing.m))

                // Password requirements checklist
                Row(
                    horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.l)
                ) {
                    PasswordRequirement(
                        text = "8+ CHARACTERS",
                        met = hasMinLength
                    )
                    PasswordRequirement(
                        text = "1 NUMBER",
                        met = hasNumber
                    )
                }

                Spacer(Modifier.height(28.dp))

                NyasaButton(
                    text = "UPDATE PASSWORD",
                    onClick = {
                        onUpdatePassword(
                            currentPassword,
                            newPassword,
                            confirmNewPassword
                        )
                    },
                    enabled = canSubmit,
                    loading = isLoading
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "You will be logged out of other devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(NyasaTheme.spacing.xl))
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun PasswordRequirement(text: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Filled.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = if (met) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp
            ),
            color = if (met) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
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
