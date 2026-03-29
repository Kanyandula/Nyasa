package com.kanyandula.nyasa.ui.auth

import android.webkit.JavascriptInterface

class WebAppInterface(
    private val callback: OnWebInteractionCallback
) {

    @JavascriptInterface
    fun onSuccess(email: String) {
        callback.onSuccess(email)
    }

    @JavascriptInterface
    fun onError(errorMessage: String) {
        callback.onError(errorMessage)
    }

    @JavascriptInterface
    fun onLoading(isLoading: Boolean) {
        callback.onLoading(isLoading)
    }

    interface OnWebInteractionCallback {
        fun onSuccess(email: String)
        fun onError(errorMessage: String)
        fun onLoading(isLoading: Boolean)
    }
}
