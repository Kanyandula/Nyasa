package com.kanyandula.nyasa.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogCreateUpdateResponse
import com.kanyandula.nyasa.api.main.responses.RESPONSE_MUST_HAVE_NYASABLOG_USER
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UploadBlogPostWorkerTest {

    private lateinit var context: Context
    private lateinit var service: NyasaBlogApiMainService
    private lateinit var blogPostDao: BlogPostDao
    private lateinit var connectivityObserver: ConnectivityObserver

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        service = mockk()
        blogPostDao = mockk(relaxed = true)
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)
    }

    @Test
    fun `invalid input fails with INVALID_INPUT reason`() = runTest {
        // missing title and body in input
        val worker = buildWorker(Data.EMPTY)

        val result = worker.doWork()

        val failure = result as ListenableWorker.Result.Failure
        assertThat(failure.outputData.getString(UploadKeys.OUTPUT_ERROR))
            .isEqualTo(UploadFailureReasons.INVALID_INPUT)
    }

    @Test
    fun `offline returns retry without hitting the API`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)
        val worker = buildWorker(validInputData())

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
        coVerify(exactly = 0) {
            service.createBlog(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `successful create inserts to DAO and returns success with slug`() = runTest {
        coEvery {
            service.createBlog(any(), any(), any(), any(), any())
        } returns Response.success(blogResponse(slug = "new-post", response = "Post created."))
        val worker = buildWorker(validInputData())

        val result = worker.doWork()

        val success = result as ListenableWorker.Result.Success
        assertThat(success.outputData.getString(UploadKeys.OUTPUT_SLUG)).isEqualTo("new-post")
        assertThat(success.outputData.getString(UploadKeys.OUTPUT_MESSAGE)).isEqualTo("Post created.")
        coVerify(exactly = 1) { blogPostDao.insert(any()) }
    }

    @Test
    fun `unauthenticated sentinel response skips DAO insert`() = runTest {
        coEvery {
            service.createBlog(any(), any(), any(), any(), any())
        } returns Response.success(
            blogResponse(slug = "unused", response = RESPONSE_MUST_HAVE_NYASABLOG_USER),
        )
        val worker = buildWorker(validInputData())

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        coVerify(exactly = 0) { blogPostDao.insert(any()) }
    }

    @Test
    fun `401 maps to terminal failure with UNAUTHORIZED reason`() = runTest {
        coEvery {
            service.createBlog(any(), any(), any(), any(), any())
        } returns Response.error(401, "".toResponseBody("application/json".toMediaTypeOrNull()))
        val worker = buildWorker(validInputData())

        val result = worker.doWork()

        val failure = result as ListenableWorker.Result.Failure
        assertThat(failure.outputData.getString(UploadKeys.OUTPUT_ERROR))
            .isEqualTo(UploadFailureReasons.UNAUTHORIZED)
    }

    @Test
    fun `5xx maps to retry`() = runTest {
        coEvery {
            service.createBlog(any(), any(), any(), any(), any())
        } returns Response.error(503, "".toResponseBody("application/json".toMediaTypeOrNull()))
        val worker = buildWorker(validInputData())

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
    }

    @Test
    fun `slug present routes to updateBlog instead of createBlog`() = runTest {
        coEvery {
            service.updateBlog(any(), any(), any(), any(), any(), any())
        } returns Response.success(blogResponse(slug = "edited", response = "Post updated."))
        val worker = buildWorker(validInputData(slug = "edited"))

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        coVerify(exactly = 1) {
            service.updateBlog(eq("edited"), any(), any(), any(), any(), any())
        }
        coVerify(exactly = 0) {
            service.createBlog(any(), any(), any(), any(), any())
        }
    }

    // region helpers

    private fun buildWorker(inputData: Data): UploadBlogPostWorker =
        TestListenableWorkerBuilder<UploadBlogPostWorker>(context)
            .setInputData(inputData)
            .setWorkerFactory(testFactory())
            .build()

    private fun testFactory(): WorkerFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
        ): ListenableWorker = UploadBlogPostWorker(
            appContext,
            workerParameters,
            service,
            blogPostDao,
            connectivityObserver,
        )
    }

    private fun validInputData(slug: String? = null): Data {
        val builder = Data.Builder()
            .putString(UploadKeys.INPUT_TITLE, "Title")
            .putString(UploadKeys.INPUT_BODY, "Body")
        if (slug != null) builder.putString(UploadKeys.INPUT_SLUG, slug)
        return builder.build()
    }

    private fun blogResponse(slug: String, response: String) = BlogCreateUpdateResponse(
        response = response,
        pk = 1,
        title = "T",
        slug = slug,
        body = "B",
        image = "https://example.com/i.jpg",
        date_updated = "2026-04-25T00:00:00Z",
        username = "u",
    )

    // endregion
}
