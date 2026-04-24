package com.kanyandula.nyasa.util

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File

/**
 * Streams [file] as a multipart body and reports upload progress.
 * Idempotent: each `writeTo` opens a fresh stream, so OkHttp is free to retry
 * within a single request (e.g. auth challenges) without sending zero bytes.
 * Progress is throttled to whole-percent transitions.
 */
class UploadStreamRequestBody(
    private val mediaType: String,
    private val file: File,
    private val onUploadProgress: (Int) -> Unit,
) : RequestBody() {

    override fun contentLength(): Long = file.length()

    override fun contentType(): MediaType? = mediaType.toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        val total = file.length().toFloat().coerceAtLeast(1f)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var lastPercent = -1
        file.inputStream().use { input ->
            var uploaded = 0L
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                val percent = (100 * uploaded / total).toInt()
                if (percent != lastPercent) {
                    lastPercent = percent
                    onUploadProgress(percent)
                }
            }
        }
    }
}
