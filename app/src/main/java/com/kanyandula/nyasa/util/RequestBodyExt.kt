package com.kanyandula.nyasa.util

import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun String.toPlainTextBody(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())

fun Uri.toMultipartImage(): MultipartBody.Part? {
    val filePath = path ?: return null
    val imageFile = File(filePath)
    if (!imageFile.exists()) return null
    val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
}
