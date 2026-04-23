package com.kanyandula.nyasa.ui.components.htmlrenderer

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

private val FALLBACK_HEIGHT = 300.dp

@Composable
internal fun WebViewBlock(
    html: String,
    modifier: Modifier = Modifier
) {
    val bgColor = MaterialTheme.colorScheme.surface.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val wrappedHtml = remember(html, bgColor, textColor) {
        """
        <!DOCTYPE html>
        <html><head>
        <meta name="viewport"
            content="width=device-width, initial-scale=1.0">
        <style>
            body {
                margin: 0; padding: 0;
                background: ${bgColor.toCssRgb()};
                color: ${textColor.toCssRgb()};
            }
            iframe, table, img {
                max-width: 100%; height: auto;
            }
            table {
                width: 100%; border-collapse: collapse;
            }
            th, td {
                border: 1px solid ${textColor.toCssRgb()};
                padding: 8px; text-align: left;
            }
        </style>
        </head><body>$html</body></html>
        """.trimIndent()
    }

    var height by remember { mutableStateOf(FALLBACK_HEIGHT) }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.javaScriptEnabled = false
                settings.allowFileAccess = false
                isVerticalScrollBarEnabled = false
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            if (webView.tag != wrappedHtml) {
                webView.tag = wrappedHtml
                webView.loadDataWithBaseURL(
                    null,
                    wrappedHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.destroy()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}

private fun Int.toCssRgb(): String {
    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    return "rgb($r,$g,$b)"
}
