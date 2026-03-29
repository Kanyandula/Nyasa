package com.kanyandula.nyasa.ui.auth.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

    DisposableEffect(Unit) {
        onDispose { onFieldsChanged(email, username) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        Text(
            text = "Join the Story",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Start your journey into the digital landscapes of Malawi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        NyasaTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(16.dp))

        NyasaTextField(
            value = username,
            onValueChange = { username = it },
            label = "Username",
            leadingIcon = Icons.Filled.Person,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(16.dp))

        NyasaTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(16.dp))

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
                if (!passwordMismatch) {
                    onRegister(email, username, password, confirmPassword)
                }
            }
        )
        Spacer(Modifier.height(32.dp))

        NyasaButton(
            text = "Register",
            onClick = { onRegister(email, username, password, confirmPassword) },
            enabled = !passwordMismatch,
            loading = isLoading,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
        )
        Spacer(Modifier.height(24.dp))

        SocialAuthRow(dividerText = "or join with")
        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
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
