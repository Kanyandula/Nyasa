@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.repository.main.CreateBlogRepository
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState.NewBlogFields
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_CREATED
import com.kanyandula.nyasa.util.toPlainTextBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class CreateBlogViewModel
@Inject
constructor(
    private val createBlogRepository: CreateBlogRepository
) : BaseViewModel<CreateBlogViewState>(CreateBlogViewState()) {

    fun createNewBlogPost(title: String, body: String, image: MultipartBody.Part) {
        val titleBody = title.toPlainTextBody()
        val bodyBody = body.toPlainTextBody()

        viewModelScope.launch {
            createBlogRepository.createNewBlogPost(titleBody, bodyBody, image)
                .collect { resource ->
                    handleResource(
                        resource,
                        onSuccess = { message ->
                            sendEvent(UiEvent.ShowSuccessDialog(message))
                            if (message == SUCCESS_BLOG_CREATED) {
                                clearNewBlogFields()
                            }
                        }
                    )
                }
        }
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val current = viewState.value.blogFields
        updateState {
            copy(
                blogFields = blogFields.copy(
                    newBlogTitle = title ?: current.newBlogTitle,
                    newBlogBody = body ?: current.newBlogBody,
                    newImageUri = uri ?: current.newImageUri
                )
            )
        }
    }

    fun clearNewBlogFields() {
        updateState { copy(blogFields = NewBlogFields()) }
    }
}
