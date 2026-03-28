package com.kanyandula.nyasa.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toPlainTextBody(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())
