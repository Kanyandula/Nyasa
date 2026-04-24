package com.kanyandula.nyasa.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Constants.RESPONSE_MUST_HAVE_NYASABLOG_UER
import com.kanyandula.nyasa.util.UploadStreamRequestBody
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File

@HiltWorker
class UploadBlogPostWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val service: NyasaBlogApiMainService,
    private val blogPostDao: BlogPostDao,
    private val connectivityObserver: ConnectivityObserver,
) : CoroutineWorker(context, params) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        UploadNotifications.createChannel(applicationContext)
        return ForegroundInfo(
            UploadKeys.NOTIFICATION_ID_ONGOING,
            UploadNotifications.ongoingNotification(applicationContext, 0),
        )
    }

    override suspend fun doWork(): Result {
        val title = inputData.getString(UploadKeys.INPUT_TITLE)
        val body = inputData.getString(UploadKeys.INPUT_BODY)
        if (title.isNullOrBlank() || body.isNullOrBlank()) {
            return failure("InvalidInput")
        }
        val category = inputData.getString(UploadKeys.INPUT_CATEGORY)
        val tagsCsv = inputData.getString(UploadKeys.INPUT_TAGS_CSV)
        val imagePath = inputData.getString(UploadKeys.INPUT_IMAGE_PATH)

        if (!connectivityObserver.isConnected.value) {
            Timber.d("UploadBlogPostWorker offline — will retry when connected")
            return Result.retry()
        }

        runCatching { setForeground(getForegroundInfo()) }
            .onFailure { Timber.w(it, "setForeground failed; continuing without foreground notification") }

        val textPlain = "text/plain".toMediaTypeOrNull()
        val titlePart = title.toRequestBody(textPlain)
        val bodyPart = body.toRequestBody(textPlain)
        val categoryPart = category?.toRequestBody(textPlain)
        val tagsPart = tagsCsv?.toRequestBody(textPlain)

        val imageFile = imagePath?.let(::File)
        if (imageFile != null && !imageFile.exists()) {
            Timber.w("Upload image missing at $imagePath")
            return failure("ImageMissing")
        }
        val imagePart = imageFile?.let {
            val stream = it.inputStream()
            val streamBody = UploadStreamRequestBody(
                mediaType = "image/jpeg",
                inputStream = stream,
                onUploadProgress = { percent ->
                    setProgressAsync(workDataOf(UploadKeys.PROGRESS_PERCENT to percent))
                },
            )
            MultipartBody.Part.createFormData("image", it.name, streamBody)
        }

        return try {
            val response = service.createBlog(titlePart, bodyPart, imagePart, categoryPart, tagsPart)
            if (!response.isSuccessful) {
                val code = response.code()
                return when {
                    code == 408 || code in 500..599 -> Result.retry()
                    else -> failure("Http$code")
                }
            }
            val payload = response.body() ?: return failure("EmptyBody")
            val post = payload.toBlogPost()
            if (payload.response != RESPONSE_MUST_HAVE_NYASABLOG_UER) {
                blogPostDao.insert(post)
            }
            imageFile?.runCatching { delete() }
            Result.success(
                workDataOf(
                    UploadKeys.OUTPUT_SLUG to post.slug,
                    UploadKeys.OUTPUT_MESSAGE to payload.response,
                ),
            )
        } catch (t: Throwable) {
            Timber.w(t, "UploadBlogPostWorker threw; retrying")
            Result.retry()
        }
    }

    private fun failure(reason: String): Result =
        Result.failure(workDataOf(UploadKeys.OUTPUT_ERROR to reason))
}