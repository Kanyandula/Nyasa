package com.kanyandula.nyasa.ui.auth.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.ui.components.ButtonStyle
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NyasaTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(NyasaTheme.spacing.xxl))

            Text(
                text = "NyasaBlog",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            SunsetHeroIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NyasaTheme.spacing.s)
            )

            Spacer(Modifier.height(NyasaTheme.spacing.xl))

            Text(
                text = "Welcome to\nNyasaBlog",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Discover the pulse of Malawian stories," +
                    " where every voice finds its horizon.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            NyasaButton(
                text = "Login",
                onClick = onLoginClick,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
            )

            Spacer(Modifier.height(12.dp))

            NyasaButton(
                text = "Register",
                onClick = onRegisterClick,
                style = ButtonStyle.Secondary
            )

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "FORGOT PASSWORD",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(NyasaTheme.spacing.xl))
        }
    }
}

@Composable
private fun SunsetHeroIllustration(modifier: Modifier = Modifier) {
    val skyTop = NyasaTheme.colors.sunsetSkyTop
    val gradientStart = NyasaTheme.colors.sunsetGradientStart
    val skyBottom = NyasaTheme.colors.sunsetSkyBottom
    val gradientEnd = NyasaTheme.colors.sunsetGradientEnd
    val textHighlight = NyasaTheme.colors.sunsetTextHighlight
    val textGlow = NyasaTheme.colors.sunsetTextGlow
    val waterReflection = NyasaTheme.colors.sunsetGradientEnd
    val patternColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky gradient — warm sunset tones
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        skyTop,
                        gradientStart,
                        skyBottom,
                        gradientEnd
                    )
                )
            )

            // Sun glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        textHighlight,
                        textGlow.copy(alpha = 0.6f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.35f
                ),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.35f
            )

            // Sun disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        textHighlight,
                        textGlow
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.1f
                ),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.1f
            )

            // Water reflection
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        waterReflection.copy(alpha = 0.5f),
                        patternColor.copy(alpha = 0.3f),
                        patternColor.copy(alpha = 0.5f)
                    ),
                    startY = h * 0.65f,
                    endY = h
                ),
                topLeft = Offset(0f, h * 0.65f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.35f)
            )

            // Chitenje dot pattern overlay
            val dotRadius = 2f
            val spacing = 20f
            val dotColor = patternColor.copy(alpha = 0.04f)
            var y = 0f
            while (y < h) {
                var x = 0f
                while (x < w) {
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    x += spacing
                }
                y += spacing
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    NyasaTheme {
        WelcomeScreen(
            onLoginClick = {},
            onRegisterClick = {},
            onForgotPasswordClick = {}
        )
    }
}
