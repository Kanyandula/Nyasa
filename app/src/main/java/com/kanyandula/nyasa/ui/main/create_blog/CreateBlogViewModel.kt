@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogNavigationEvent
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState.NewBlogFields
import com.kanyandula.nyasa.util.BlogUtils
import com.kanyandula.nyasa.util.analytics.AnalyticsEvent
import com.kanyandula.nyasa.util.analytics.AnalyticsTracker
import com.kanyandula.nyasa.work.BlogUploadEnqueuer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateBlogViewModel
@Inject
constructor(
    private val blogUploadEnqueuer: BlogUploadEnqueuer,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val analyticsTracker: AnalyticsTracker
) : BaseViewModel<CreateBlogViewState>(CreateBlogViewState()) {

    init {
        loadCategories()
    }

    fun createNewBlogPost(title: String, body: String, imageUri: Uri?) {
        if (isLoading.value) return // double-tap guard
        setLoading(true)
        val fields = viewState.value.blogFields
        val tagsCsv = BlogUtils.parseTags(fields.tags)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(",")

        viewModelScope.launch {
            try {
                blogUploadEnqueuer.enqueueCreate(
                    title = title,
                    body = body,
                    imageUri = imageUri,
                    category = fields.category,
                    tagsCsv = tagsCsv
                )
                // H6: analytics fires on enqueue (attempt). Terminal state is observed at
                // the Activity level via WorkManager WorkInfo for the success/failure Toast.
                analyticsTracker.trackEvent(AnalyticsEvent.CreatePost(fields.category))
                clearNewBlogFields()
                sendEvent(CreateBlogNavigationEvent.BlogCreated)
            } finally {
                setLoading(false)
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
