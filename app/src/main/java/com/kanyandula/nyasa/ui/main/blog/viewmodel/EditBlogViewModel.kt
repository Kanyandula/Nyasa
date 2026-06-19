package com.kanyandula.nyasa.ui.main.blog.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.blog.GetBlogPostBySlugUseCase
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.state.UpdateBlogUiState
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.BlogUtils
import com.kanyandula.nyasa.util.toUserMessage
import com.kanyandula.nyasa.work.BlogUploadEnqueuer
import com.mohamedrejeb.richeditor.model.RichTextState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Per-screen ViewModel for the Edit Blog flow, scoped to the `BlogEdit` nav entry. It owns its own
 * draft state and reloads the post by slug, so editing no longer borrows the MainGraph-scoped
 * [BlogViewModel] or depends on the Detail screen seeding fields before navigation.
 */
@HiltViewModel
class EditBlogViewModel
@Inject
constructor(
    private val blogUploadEnqueuer: BlogUploadEnqueuer,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBlogPostBySlugUseCase: GetBlogPostBySlugUseCase
) : BaseViewModel<UpdateBlogUiState>(UpdateBlogUiState()) {

    // Hoisted so a recomposition or rotation can't reset the editor mid-edit.
    val editBodyState: RichTextState = RichTextState()

    private var loadedSlug: String? = null
    private var loadJob: Job? = null
    private var updateJob: Job? = null

    init {
        loadCategories()
    }

    /**
     * Loads the post for [slug] and seeds the draft once. A repeat call for the same slug (e.g. the
     * route's `LaunchedEffect` re-running after a config change) is ignored so in-progress edits and
     * the editor body are preserved. The slug is marked loaded only once the fetch returns — not on
     * cancellation — so a missing post shows its error dialog once while an interrupted fetch can
     * still be retried.
     */
    fun loadBlogForEdit(slug: String) {
        if (loadedSlug == slug) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val blogPost = getBlogPostBySlugUseCase(slug)
            loadedSlug = slug
            if (blogPost != null) {
                updateState {
                    copy(
                        updatedBlogTitle = blogPost.title,
                        updatedImageUri = null,
                        originalImageUrl = blogPost.image,
                        updatedCategory = blogPost.category,
                        updatedTags = blogPost.tags
                    )
                }
                editBodyState.setHtml(blogPost.body)
            } else {
                sendEvent(UiEvent.ShowErrorDialog(AppError.NotFound.toUserMessage()))
            }
        }
    }

    fun getUpdatedBlogUri(): Uri? = viewState.value.updatedImageUri

    fun setUpdatedBlogFields(
        title: String? = null,
        uri: Uri? = null,
        originalImageUrl: String? = null,
        category: String? = null,
        tags: String? = null
    ) {
        updateState {
            copy(
                updatedBlogTitle = title ?: updatedBlogTitle,
                updatedImageUri = uri ?: updatedImageUri,
                originalImageUrl = originalImageUrl ?: this.originalImageUrl,
                updatedCategory = category ?: updatedCategory,
                updatedTags = tags ?: updatedTags
            )
        }
    }

    fun clearUpdatedImageUri() {
        updateState { copy(updatedImageUri = null) }
    }

    fun setUpdatedCategory(category: String?) {
        updateState { copy(updatedCategory = category) }
    }

    fun setUpdatedTags(tags: String) {
        updateState { copy(updatedTags = tags) }
    }

    fun updateBlogPost(slug: String, title: String, body: String, imageUri: Uri?) {
        if (isLoading.value) return // double-tap guard
        setLoading(true)
        val state = viewState.value
        val tagsCsv = BlogUtils.parseTags(state.updatedTags)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(",")

        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                blogUploadEnqueuer.enqueueUpdate(
                    slug = slug,
                    title = title,
                    body = body,
                    imageUri = imageUri,
                    category = state.updatedCategory,
                    tagsCsv = tagsCsv
                )
                // H6 PR B: emit nav event on enqueue (attempt). Success/failure surfaces via the
                // WorkInfo observer in MainActivity (Toast) and the DAO insert the worker performs
                // on success propagating through Paging.
                clearUpdatedImageUri()
                sendEvent(BlogNavigationEvent.BlogUpdateSuccess)
            } finally {
                setLoading(false)
            }
        }
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
