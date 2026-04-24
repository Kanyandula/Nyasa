package com.kanyandula.nyasa.work

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kanyandula.nyasa.util.toCompressedUploadFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlogUploadEnqueuer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Stages [imageUri] (compressing on Dispatchers.IO) and enqueues an [UploadBlogPostWorker].
     * Suspends only for the duration of compression — enqueuing itself is fast.
     */
    suspend fun enqueueCreate(
        title: String,
        body: String,
        imageUri: Uri?,
        category: String?,
        tagsCsv: String?,
    ): UUID {
        val imagePath = imageUri?.let { stageImage(it) }

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

    private suspend fun stageImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching { uri.toCompressedUploadFile(context).absolutePath }
            .onFailure { Timber.w(it, "Failed to stage upload image; enqueuing without image") }
            .getOrNull()
    }
}