package com.kanyandula.nyasa.ui.auth.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.Primary
import com.kanyandula.nyasa.ui.theme.SunsetOrange

@Composable
fun LoginScreen(
    initialEmail: String,
    isLoading: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onBackClick: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var password by rememberSaveable { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose { onEmailChanged(email) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Top bar: back arrow + branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NyasaBlog",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "DIGITAL BAOBAB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.weight(1f))
                // Invisible spacer to balance the back button
                Box(modifier = Modifier.size(48.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Sunset banner strip
            SunsetBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Please enter your details to sign in.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            // Email field with uppercase label
            Text(
                text = "EMAIL ADDRESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            NyasaTextField(
                value = email,
                onValueChange = { email = it },
                label = "name@example.com",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(20.dp))

            // Password label row with forgot link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PASSWORD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "FORGOT PASSWORD?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable(onClick = onForgotPassword)
                )
            }
            Spacer(Modifier.height(8.dp))
            NyasaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { onLogin(email, password) }
            )

            Spacer(Modifier.height(28.dp))

            NyasaButton(
                text = "Login",
                onClick = { onLogin(email, password) },
                loading = isLoading,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )

            Spacer(Modifier.height(28.dp))

            SocialAuthRow(dividerText = "OR CONTINUE WITH")

            Spacer(Modifier.height(20.dp))

            // Social buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Google",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "Apple",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Sign up",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SunsetBanner(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.aspectRatio(16f / 7f)
    ) {
        val w = size.width
        val h = size.height

        // Sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF87CEEB),
                    Color(0xFFFDB777),
                    Color(0xFFF4A460),
                    Color(0xFFE8883C)
                )
            )
        )

        // Sun glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF3E0),
                    Color(0xFFFFCC80).copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.25f
            ),
            center = Offset(w * 0.5f, h * 0.4f),
            radius = w * 0.25f
        )

        // Sun disc
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFE0B2)),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.06f
            ),
            center = Offset(w * 0.5f, h * 0.4f),
            radius = w * 0.06f
        )

        // Water reflection
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    SunsetOrange.copy(alpha = 0.5f),
                    Primary.copy(alpha = 0.4f),
                    Primary.copy(alpha = 0.6f)
                ),
                startY = h * 0.65f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.65f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    NyasaTheme {
        LoginScreen(
            initialEmail = "",
            isLoading = false,
            onLogin = { _, _ -> },
            onForgotPassword = {},
            onNavigateToRegister = {},
            onEmailChanged = {},
            onBackClick = {}
        )
    }
}
