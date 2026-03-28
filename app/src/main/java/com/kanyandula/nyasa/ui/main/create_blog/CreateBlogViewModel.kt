@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.createblog.CreateBlogPostUseCase
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState.NewBlogFields
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_CREATED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateBlogViewModel
@Inject
constructor(
    private val createBlogPostUseCase: CreateBlogPostUseCase
) : BaseViewModel<CreateBlogViewState>(CreateBlogViewState()) {

    fun createNewBlogPost(title: String, body: String, imageUri: Uri?) {
        viewModelScope.launch {
            createBlogPostUseCase(title, body, imageUri)
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
