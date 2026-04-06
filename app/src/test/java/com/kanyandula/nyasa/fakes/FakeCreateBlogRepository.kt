package com.kanyandula.nyasa.fakes

import android.net.Uri
import com.kanyandula.nyasa.domain.repository.CreateBlogRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

class FakeCreateBlogRepository : CreateBlogRepository {

    var createResult: Resource<String> = Resource.Success("Blog post created")

    override fun createNewBlogPost(
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<String>> = fakeResourceFlow { createResult }
}
