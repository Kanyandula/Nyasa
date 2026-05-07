package com.kanyandula.nyasa.domain.usecase.blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetFeaturedBlogPostUseCaseTest {

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var useCase: GetFeaturedBlogPostUseCase

    @Before
    fun setup() {
        fakeRepository = FakeBlogRepository()
        useCase = GetFeaturedBlogPostUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with featured post`() = runTest {
        val featured = BlogPost(
            pk = 105,
            title = "Malawi Cichlids",
            slug = "malawi-cichlids",
            body = "body",
            image = "img",
            date_updated = 0L,
            username = "editor",
            is_featured = true
        )
        fakeRepository.featuredBlogPostResult = Resource.Success(featured)

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data?.pk).isEqualTo(105)
            assertThat(success.data?.is_featured).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then success with null when nothing featured`() = runTest {
        fakeRepository.featuredBlogPostResult = Resource.Success(null)

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).isNull()
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.featuredBlogPostResult =
            Resource.Error(AppError.Unknown(RuntimeException("boom")))

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            assertThat(awaitItem()).isInstanceOf(Resource.Error::class.java)
            awaitComplete()
        }
    }
}
