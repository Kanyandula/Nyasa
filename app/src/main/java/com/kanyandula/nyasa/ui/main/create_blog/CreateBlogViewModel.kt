@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.domain.usecase.createblog.CreateBlogPostUseCase
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState.NewBlogFields
import com.kanyandula.nyasa.util.BlogUtils
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_CREATED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateBlogViewModel
@Inject
constructor(
    private val createBlogPostUseCase: CreateBlogPostUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : BaseViewModel<CreateBlogViewState>(CreateBlogViewState()) {

    init {
        loadCategories()
    }

    fun createNewBlogPost(title: String, body: String, imageUri: Uri?) {
        val fields = viewState.value.blogFields
        val tagsList = BlogUtils.parseTags(fields.tags).takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            createBlogPostUseCase(title, body, imageUri, fields.category, tagsList)
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

    fun setNewBlogFields(
        title: String? = null,
        body: String? = null,
        uri: Uri? = null,
        category: String? = null,
        tags: String? = null
    ) {
        val current = viewState.value.blogFields
        updateState {
            copy(
                blogFields = blogFields.copy(
                    newBlogTitle = title ?: current.newBlogTitle,
                    newBlogBody = body ?: current.newBlogBody,
                    newImageUri = uri ?: current.newImageUri,
                    category = category ?: current.category,
                    tags = tags ?: current.tags
                )
            )
        }
    }

    fun setCategory(category: String?) {
        updateState {
            copy(blogFields = blogFields.copy(category = category))
        }
    }

    fun setTags(tags: String) {
        updateState {
            copy(blogFields = blogFields.copy(tags = tags))
        }
    }

    fun clearNewBlogFields() {
        updateState { copy(blogFields = NewBlogFields()) }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { categories ->
                        updateState { copy(categories = categories) }
                    }
                )
            }
        }
    }
}
