package com.kanyandula.nyasa.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogCreateUpdateResponse
import com.kanyandula.nyasa.api.main.responses.RESPONSE_MUST_HAVE_NYASABLOG_USER
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.UploadStreamRequestBody
import com.kanyandula.nyasa.util.safeApiCall
import com.kanyandula.nyasa.util.toPlainTextBody
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import okhttp3.MultipartBody
import retrofit2.Response
import timber.log.Timber

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
        val input = UploadInput.from(inputData)
            ?: return failureFor(UploadFailureReasons.INVALID_INPUT)
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
            return failureFor(UploadFailureReasons.IMAGE_MISSING)
        }
        return runLoggingCatch("upload") { performUpload(input) } ?: Result.retry()
    }

    private suspend fun performUpload(input: UploadInput): Result =
        when (val resource = safeApiCall { callApi(input) }) {
            is Resource.Success -> handleSuccess(resource.data)
            is Resource.Error -> handleError(resource.error)
            // safeApiCall never emits Loading. If that contract changes, fail terminally
            // so the worker doesn't loop forever — we'd see this in Crashlytics.
            is Resource.Loading -> failureFor(UploadFailureReasons.UNEXPECTED_LOADING)
        }

    private suspend fun handleSuccess(payload: BlogCreateUpdateResponse): Result {
        val post = payload.toBlogPost()
        if (payload.response != RESPONSE_MUST_HAVE_NYASABLOG_USER) {
            blogPostDao.insert(post)
        }
        return Result.success(
            workDataOf(
                UploadKeys.OUTPUT_SLUG to post.slug,
                UploadKeys.OUTPUT_MESSAGE to payload.response,
            ),
        )
    }

    private fun handleError(error: AppError): Result {
        Timber.w((error as? AppError.Unknown)?.cause, "Upload failed: %s", error)
        return if (error.isTransient()) Result.retry() else failureFor(error.reasonCode())
    }

    private suspend fun callApi(input: UploadInput): Response<BlogCreateUpdateResponse> {
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
        val titlePart = input.title.toPlainTextBody()
        val bodyPart = input.body.toPlainTextBody()
        val categoryPart = input.category?.toPlainTextBody()
        val tagsPart = input.tagsCsv?.toPlainTextBody()
        return if (input.slug != null) {
            service.updateBlog(input.slug, titlePart, bodyPart, imagePart, categoryPart, tagsPart)
        } else {
            service.createBlog(titlePart, bodyPart, imagePart, categoryPart, tagsPart)
        }
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
}
