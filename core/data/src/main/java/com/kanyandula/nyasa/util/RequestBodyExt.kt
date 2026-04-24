package com.kanyandula.nyasa.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private const val UPLOAD_TEMP_PREFIX = "nyasa_upload_"
private const val COMPRESSOR_CACHE_DIR = "compressor"
private const val MAX_RAW_SIZE_BYTES = 20L * 1024 * 1024
private const val MAX_COMPRESSED_SIZE_BYTES = 10L * 1024 * 1024
private const val COMPRESSION_QUALITY = 80
private const val MAX_RESOLUTION = 1920

fun String.toPlainTextBody(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())

suspend fun Uri.toCompressedMultipartImage(context: Context): MultipartBody.Part {
    val compressed = toCompressedUploadFile(context)
    val requestBody = compressed.asRequestBody("image/jpeg".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", "blog_image.jpg", requestBody)
}

/**
 * Stages [this] image URI as a compressed JPEG on disk and returns the resulting [File].
 * Used by both the foreground multipart path ([toCompressedMultipartImage]) and the H6
 * background upload path (BlogUploadEnqueuer). Caller owns the returned file's lifecycle.
 */
suspend fun Uri.toCompressedUploadFile(context: Context): File {
    cleanupStaleTempFiles(context)

    val tempFile = copyUriToTempFile(context, this)
    try {
        validatePreCompressionSize(tempFile)
        val compressed = compressImage(context, tempFile)
        tempFile.delete()
        validatePostCompressionSize(compressed)
        return compressed
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        tempFile.delete()
        throw e
    }
}

private fun cleanupStaleTempFiles(context: Context) {
    context.cacheDir.listFiles { file ->
        file.name.startsWith(UPLOAD_TEMP_PREFIX)
    }?.forEach { it.delete() }

    File(context.cacheDir, COMPRESSOR_CACHE_DIR).let { dir ->
        if (dir.isDirectory) dir.listFiles()?.forEach { it.delete() }
    }
}

private fun copyUriToTempFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: error("Failed to read image: could not open file")

    val tempFile = File.createTempFile(UPLOAD_TEMP_PREFIX, ".jpg", context.cacheDir)
    inputStream.use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}

private fun validatePreCompressionSize(file: File) {
    if (file.length() > MAX_RAW_SIZE_BYTES) {
        error("Image is too large. Please select an image under 20MB.")
    }
}

private suspend fun compressImage(context: Context, file: File): File =
    Compressor.compress(context, file) {
        quality(COMPRESSION_QUALITY)
        format(Bitmap.CompressFormat.JPEG)
        resolution(MAX_RESOLUTION, MAX_RESOLUTION)
    }

private fun validatePostCompressionSize(file: File) {
    if (file.length() > MAX_COMPRESSED_SIZE_BYTES) {
        error("Image is still too large after compression. Please select a smaller image.")
    }
}
