package com.kanyandula.nyasa.work

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlogUploadEnqueuer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueueCreate(
        title: String,
        body: String,
        imageUri: Uri?,
        category: String?,
        tagsCsv: String?,
    ): UUID {
        val imagePath = imageUri?.let(::copyToUploadsCache)

        val request = OneTimeWorkRequestBuilder<UploadBlogPostWorker>()
            .addTag(UploadKeys.WORK_TAG_UPLOAD)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setInputData(
                workDataOf(
                    UploadKeys.INPUT_TITLE to title,
                    UploadKeys.INPUT_BODY to body,
                    UploadKeys.INPUT_CATEGORY to category,
                    UploadKeys.INPUT_TAGS_CSV to tagsCsv,
                    UploadKeys.INPUT_IMAGE_PATH to imagePath,
                ),
            )
            .build()

        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }

    private fun copyToUploadsCache(uri: Uri): String? = try {
        val dir = File(context.cacheDir, "uploads").apply { mkdirs() }
        val dest = File(dir, "upload_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { src ->
            dest.outputStream().use { out -> src.copyTo(out) }
        }
        dest.absolutePath
    } catch (t: Throwable) {
        Timber.w(t, "Failed to stage upload image; enqueuing without image")
        null
    }
}