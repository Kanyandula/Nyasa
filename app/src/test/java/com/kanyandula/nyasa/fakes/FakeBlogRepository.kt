package com.kanyandula.nyasa.fakes

import android.net.Uri
import androidx.paging.PagingData
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBlogRepository : BlogRepository {

    var blogPosts: List<BlogPost> = emptyList()
    var isAuthorResult: Resource<Boolean> = Resource.Success(true)
    var deleteResult: Resource<String> = Resource.Success("Deleted")
    var updateResult: Resource<BlogPost> = Resource.Success(
        BlogPost(
            pk = 1,
            title = "Updated",
            slug = "updated",
            body = "Updated body",
            image = "",
            date_updated = 0L,
            username = "testuser"
        )
    )
    var blogPostBySlug: BlogPost? = null

    override fun getBlogPagingData(
        query: String,
        filterAndOrder: String
    ): Flow<PagingData<BlogPost>> = flowOf(PagingData.from(blogPosts))

    override fun isAuthorOfBlogPost(slug: String): Flow<Resource<Boolean>> =
        fakeResourceFlow { isAuthorResult }

    override fun deleteBlogPost(blogPost: BlogPost): Flow<Resource<String>> =
        fakeResourceFlow { deleteResult }

    override fun updateBlogPost(
        slug: String,
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<BlogPost>> = fakeResourceFlow { updateResult }

    override suspend fun getBlogPostBySlug(slug: String): BlogPost? = blogPostBySlug
}
