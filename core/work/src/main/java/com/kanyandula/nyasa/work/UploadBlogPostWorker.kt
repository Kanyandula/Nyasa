package com.kanyandula.nyasa.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogCreateUpdateResponse
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Constants.RESPONSE_MUST_HAVE_NYASABLOG_UER
import com.kanyandula.nyasa.util.UploadStreamRequestBody
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
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

    override suspend fun getForegroundInfo(): ForegroundInfo = ForegroundInfo(
        UploadKeys.NOTIFICATION_ID_ONGOING,
        UploadNotifications.ongoingNotification(applicationContext, 0),
    )

    override suspend fun doWork(): Result {
        val title = inputData.getString(UploadKeys.INPUT_TITLE)
        val body = inputData.getString(UploadKeys.INPUT_BODY)
        if (title.isNullOrBlank() || body.isNullOrBlank()) {
            return failure("InvalidInput", imagePath = null)
        }
        val category = inputData.getString(UploadKeys.INPUT_CATEGORY)
        val tagsCsv = inputData.getString(UploadKeys.INPUT_TAGS_CSV)
        val imagePath = inputData.getString(UploadKeys.INPUT_IMAGE_PATH)

        if (!connectivityObserver.isConnected.value) {
            Timber.d("UploadBlogPostWorker offline — will retry when connected")
            return Result.retry()
        }

        try {
            setForeground(getForegroundInfo())
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") t: Throwable) {
            Timber.w(t, "setForeground failed; continuing without foreground notification")
        }

        val imageFile = imagePath?.let(::File)
        if (imageFile != null && !imageFile.exists()) {
            Timber.w("Upload image missing at $imagePath")
            return failure("ImageMissing", imagePath)
        }

        return try {
            performUpload(title, body, category, tagsCsv, imageFile)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") t: Throwable) {
            Timber.w(t, "UploadBlogPostWorker threw; retrying")
            Result.retry()
        }
    }

    private suspend fun performUpload(
        title: String,
        body: String,
        category: String?,
        tagsCsv: String?,
        imageFile: File?,
    ): Result {
        val response = callCreateBlog(title, body, category, tagsCsv, imageFile)
        if (!response.isSuccessful) {
            val code = response.code()
            return when {
                code == 408 || code in 500..599 -> Result.retry()
                else -> failure("Http$code", imageFile?.absolutePath)
            }
        }
        val payload = response.body() ?: return failure("EmptyBody", imageFile?.absolutePath)
        val post = payload.toBlogPost()
        if (payload.response != RESPONSE_MUST_HAVE_NYASABLOG_UER) {
            blogPostDao.insert(post)
        }
        imageFile?.runCatching { delete() }
        return Result.success(
            workDataOf(
                UploadKeys.OUTPUT_SLUG to post.slug,
                UploadKeys.OUTPUT_MESSAGE to payload.response,
            ),
        )
    }

    private suspend fun callCreateBlog(
        title: String,
        body: String,
        category: String?,
        tagsCsv: String?,
        imageFile: File?,
    ): Response<BlogCreateUpdateResponse> {
        val textPlain = "text/plain".toMediaTypeOrNull()
        val titlePart = title.toRequestBody(textPlain)
        val bodyPart = body.toRequestBody(textPlain)
        val categoryPart = category?.toRequestBody(textPlain)
        val tagsPart = tagsCsv?.toRequestBody(textPlain)
        val imagePart = imageFile?.let { file ->
            val streamBody = UploadStreamRequestBody(
                mediaType = "image/jpeg",
                inputStream = file.inputStream(),
                onUploadProgress = { percent ->
                    setProgressAsync(workDataOf(UploadKeys.PROGRESS_PERCENT to percent))
                },
            )
            MultipartBody.Part.createFormData("image", file.name, streamBody)
        }
        return service.createBlog(titlePart, bodyPart, imagePart, categoryPart, tagsPart)
    }

    private fun failure(reason: String, imagePath: String?): Result {
        imagePath?.let { File(it).runCatching { delete() } }
        return Result.failure(workDataOf(UploadKeys.OUTPUT_ERROR to reason))
    }
}
