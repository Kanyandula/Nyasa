package com.kanyandula.nyasa.ui.auth.composables

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kanyandula.nyasa.ui.auth.WebAppInterface
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.Constants
import com.kanyandula.nyasa.util.analytics.TrackScreen

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onError: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit
) {
    TrackScreen("ForgotPassword")
    var resetLinkSent by rememberSaveable { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    DisposableEffect(Unit) {
        onDispose {
            handler.removeCallbacksAndMessages(null)
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with back arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = NyasaTheme.spacing.m, top = NyasaTheme.spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            AnimatedContent(
                targetState = resetLinkSent,
                label = "forgot_password_transition"
            ) { linkSent ->
                if (linkSent) {
                    PasswordResetSuccessContent(onNavigateBack = onNavigateBack)
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Icon + header section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = NyasaTheme.spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(NyasaTheme.spacing.m))

                            // Lock reset icon in circle
                            Box(
                                modifier = Modifier
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

                            Spacer(Modifier.height(NyasaTheme.spacing.l))

                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Enter your email to receive a reset link." +
                                    " We\u2019ll help you get back to your" +
                                    " stories in no time.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(NyasaTheme.spacing.l))

                        // WebView for Django password reset form
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    webViewRef = this
                                    settings.javaScriptEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(
                                            view: WebView?,
                                            url: String?
                                        ) {
                                            super.onPageFinished(view, url)
                                            handler.post { onLoadingChanged(false) }
                                        }
                                    }
                                    addJavascriptInterface(
                                        WebAppInterface(
                                            object :
                                                WebAppInterface.OnWebInteractionCallback {
                                                override fun onSuccess(email: String) {
                                                    handler.post { resetLinkSent = true }
                                                }

                                                override fun onError(
                                                    errorMessage: String
                                                ) {
                                                    handler.post { onError(errorMessage) }
                                                }

                                                override fun onLoading(
                                                    isLoading: Boolean
                                                ) {
                                                    handler.post {
                                                        onLoadingChanged(isLoading)
                                                    }
                                                }
                                            }
                                        ),
                                        "AndroidTextListener"
                                    )
                                    onLoadingChanged(true)
                                    loadUrl(Constants.PASSWORD_RESET_URL)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = NyasaTheme.spacing.m)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordResetSuccessContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(NyasaTheme.spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(NyasaTheme.spacing.l))
        Text(
            text = "Password Reset Email Sent",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Check your email for a link to reset your password.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(NyasaTheme.spacing.xl))
        NyasaButton(
            text = "Return to Login",
            onClick = onNavigateBack
        )
    }
}
