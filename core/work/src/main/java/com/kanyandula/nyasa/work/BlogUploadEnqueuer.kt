package com.kanyandula.nyasa.work

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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
    /** POST blog/create — fire-and-forget, multiple drafts can be in flight simultaneously. */
    suspend fun enqueueCreate(
        title: String,
        body: String,
        imageUri: Uri?,
        category: String?,
        tagsCsv: String?,
    ): UUID {
        val request = buildRequest(
            title = title,
            body = body,
            imageUri = imageUri,
            category = category,
            tagsCsv = tagsCsv,
            slug = null,
        )
        WorkManager.getInstance(context).enqueue(request)
        return request.id
    }

    /**
     * PUT blog/{slug}/update — uniquely keyed on slug with REPLACE so rapid edits to the
     * same post collapse to the latest one (older pending edits are cancelled).
     */
    suspend fun enqueueUpdate(
        slug: String,
        title: String,
        body: String,
        imageUri: Uri?,
        category: String?,
        tagsCsv: String?,
    ): UUID {
        val request = buildRequest(
            title = title,
            body = body,
            imageUri = imageUri,
            category = category,
            tagsCsv = tagsCsv,
            slug = slug,
        )
        WorkManager.getInstance(context).enqueueUniqueWork(
            "$WORK_NAME_UPDATE_PREFIX$slug",
            ExistingWorkPolicy.REPLACE,
            request,
        )
        return request.id
    }

    private suspend fun buildRequest(
        title: String,
        body: String,
        imageUri: Uri?,
        category: String?,
        tagsCsv: String?,
        slug: String?,
    ): OneTimeWorkRequest {
        val imagePath = imageUri?.let { stageImage(it) }
        return OneTimeWorkRequestBuilder<UploadBlogPostWorker>()
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
                    UploadKeys.INPUT_SLUG to slug,
                ),
            )
            .build()
    }

    private suspend fun stageImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching { uri.toCompressedUploadFile(context).absolutePath }
            .onFailure { Timber.w(it, "Failed to stage upload image; enqueuing without image") }
            .getOrNull()
    }

    companion object {
        private const val WORK_NAME_UPDATE_PREFIX = "blog_update_"
    }
}