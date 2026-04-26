package com.kanyandula.nyasa.work

import androidx.work.Data
import java.io.File

/**
 * Parsed representation of [UploadBlogPostWorker]'s `inputData`. Lifted to a
 * top-level type so tests can construct and parse instances without needing
 * the full `WorkerParameters` harness.
 *
 * `slug == null` means create-post; non-null means update of that slug.
 */
internal data class UploadInput(
    val title: String,
    val body: String,
    val category: String?,
    val tagsCsv: String?,
    val imageFile: File?,
    val slug: String?,
) {
    companion object {
        fun from(data: Data): UploadInput? {
            val title = data.getString(UploadKeys.INPUT_TITLE)
            val body = data.getString(UploadKeys.INPUT_BODY)
            if (title.isNullOrBlank() || body.isNullOrBlank()) return null
            return UploadInput(
                title = title,
                body = body,
                category = data.getString(UploadKeys.INPUT_CATEGORY),
                tagsCsv = data.getString(UploadKeys.INPUT_TAGS_CSV),
                imageFile = data.getString(UploadKeys.INPUT_IMAGE_PATH)?.let(::File),
                slug = data.getString(UploadKeys.INPUT_SLUG),
            )
        }
    }
}
