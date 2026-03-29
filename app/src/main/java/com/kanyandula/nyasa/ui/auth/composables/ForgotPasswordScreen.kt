package com.kanyandula.nyasa.ui.auth.composables

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.kanyandula.nyasa.util.Constants

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onError: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit
) {
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

    AnimatedContent(
        targetState = resetLinkSent,
        label = "forgot_password_transition"
    ) { linkSent ->
        if (linkSent) {
            PasswordResetSuccessContent(onNavigateBack = onNavigateBack)
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewRef = this
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                handler.post { onLoadingChanged(false) }
                            }
                        }
                        addJavascriptInterface(
                            WebAppInterface(
                                object : WebAppInterface.OnWebInteractionCallback {
                                    override fun onSuccess(email: String) {
                                        handler.post { resetLinkSent = true }
                                    }

                                    override fun onError(errorMessage: String) {
                                        handler.post { onError(errorMessage) }
                                    }

                                    override fun onLoading(isLoading: Boolean) {
                                        handler.post { onLoadingChanged(isLoading) }
                                    }
                                }
                            ),
                            "AndroidTextListener"
                        )
                        onLoadingChanged(true)
                        loadUrl(Constants.PASSWORD_RESET_URL)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PasswordResetSuccessContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
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
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onNavigateBack) {
            Text(
                text = "Return to Login",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
