package com.kanyandula.nyasa.ui.auth.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun RegisterScreen(
    initialEmail: String,
    initialUsername: String,
    isLoading: Boolean,
    onRegister: (email: String, username: String, password: String, confirmPassword: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onFieldsChanged: (email: String, username: String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var username by rememberSaveable { mutableStateOf(initialUsername) }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

    DisposableEffect(Unit) {
        onDispose { onFieldsChanged(email, username) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NyasaTheme.spacing.xl)
        ) {
            Spacer(Modifier.height(NyasaTheme.spacing.xxl))

            // Branding
            Text(
                text = "NyasaBlog",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(NyasaTheme.spacing.xs))
            Text(
                text = "JOIN THE STORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(NyasaTheme.spacing.s))
            Text(
                text = "Start your journey into the digital landscapes of Malawi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            NyasaTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Filled.AlternateEmail,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(NyasaTheme.spacing.m))

            NyasaTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                leadingIcon = Icons.Filled.Person,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(NyasaTheme.spacing.m))

            NyasaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(NyasaTheme.spacing.m))

            NyasaTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                isError = passwordMismatch,
                errorMessage = if (passwordMismatch) "Passwords do not match" else null,
                imeAction = ImeAction.Done,
                onImeAction = {
                    if (!passwordMismatch && termsAccepted) {
                        onRegister(email, username, password, confirmPassword)
                    }
                }
            )

            Spacer(Modifier.height(NyasaTheme.spacing.m))

            // Terms checkbox
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(
                            SpanStyle(color = MaterialTheme.colorScheme.primary)
                        ) {
                            append("Terms of Service")
                        }
                        append(" and ")
                        withStyle(
                            SpanStyle(color = MaterialTheme.colorScheme.primary)
                        ) {
                            append("Privacy Policy")
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            NyasaButton(
                text = "Register",
                onClick = { onRegister(email, username, password, confirmPassword) },
                enabled = !passwordMismatch && termsAccepted,
                loading = isLoading,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            SocialAuthRow(dividerText = "OR JOIN WITH")

            Spacer(Modifier.height(20.dp))

            // Social buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Google",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = NyasaTheme.spacing.m)
                )
                Text(
                    text = "Facebook",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = NyasaTheme.spacing.m)
                )
            }

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(NyasaTheme.spacing.xl))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    NyasaTheme {
        RegisterScreen(
            initialEmail = "",
            initialUsername = "",
            isLoading = false,
            onRegister = { _, _, _, _ -> },
            onNavigateToLogin = {},
            onFieldsChanged = { _, _ -> }
        )
    }
}
