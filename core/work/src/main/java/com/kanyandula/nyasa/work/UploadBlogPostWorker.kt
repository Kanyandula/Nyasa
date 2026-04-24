package com.kanyandula.nyasa.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
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
import com.kanyandula.nyasa.util.toPlainTextBody
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import okhttp3.MultipartBody
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

    override suspend fun getForegroundInfo(): ForegroundInfo =
        UploadNotifications.ongoingForegroundInfo(applicationContext, progressPercent = 0)

    override suspend fun doWork(): Result {
        val input = UploadInput.from(inputData) ?: return failureFor("InvalidInput")
        val result = computeResult(input)
        if (result !is Result.Retry) input.imageFile?.runCatching { delete() }
        return result
    }

    private suspend fun computeResult(input: UploadInput): Result {
        if (!connectivityObserver.isConnected.value) {
            Timber.d("UploadBlogPostWorker offline — will retry when connected")
            return Result.retry()
        }
        runLoggingCatch("setForeground") { setForeground(getForegroundInfo()) }
        if (input.imageFile != null && !input.imageFile.exists()) {
            Timber.w("Upload image missing at ${input.imageFile}")
            return failureFor("ImageMissing")
        }
        return runLoggingCatch("upload") { performUpload(input) } ?: Result.retry()
    }

    private suspend fun performUpload(input: UploadInput): Result {
        val response = callCreateBlog(input)
        if (!response.isSuccessful) {
            val code = response.code()
            return when {
                code == 408 || code in 500..599 -> Result.retry()
                else -> failureFor("Http$code")
            }
        }
        val payload = response.body() ?: return failureFor("EmptyBody")
        val post = payload.toBlogPost()
        if (payload.response != RESPONSE_MUST_HAVE_NYASABLOG_UER) {
            blogPostDao.insert(post)
        }
        return Result.success(
            workDataOf(
                UploadKeys.OUTPUT_SLUG to post.slug,
                UploadKeys.OUTPUT_MESSAGE to payload.response,
            ),
        )
    }

    private suspend fun callCreateBlog(input: UploadInput): Response<BlogCreateUpdateResponse> {
        val imagePart = input.imageFile?.let { file ->
            val streamBody = UploadStreamRequestBody(
                mediaType = "image/jpeg",
                file = file,
                onUploadProgress = { percent ->
                    setProgressAsync(workDataOf(UploadKeys.PROGRESS_PERCENT to percent))
                },
            )
            MultipartBody.Part.createFormData("image", file.name, streamBody)
        }
        return service.createBlog(
            title = input.title.toPlainTextBody(),
            body = input.body.toPlainTextBody(),
            image = imagePart,
            category = input.category?.toPlainTextBody(),
            tags = input.tagsCsv?.toPlainTextBody(),
        )
    }

    private fun failureFor(reason: String): Result =
        Result.failure(workDataOf(UploadKeys.OUTPUT_ERROR to reason))

    private suspend fun <T> runLoggingCatch(label: String, block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") t: Throwable) {
        Timber.w(t, "$label failed")
        null
    }

    private data class UploadInput(
        val title: String,
        val body: String,
        val category: String?,
        val tagsCsv: String?,
        val imageFile: File?,
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
                )
            }
        }
    }
}
