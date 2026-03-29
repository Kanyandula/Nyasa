package com.kanyandula.nyasa.ui.main.blog.viewmodel

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kanyandula.nyasa.domain.usecase.blog.DeleteBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.GetBlogPostBySlugUseCase
import com.kanyandula.nyasa.domain.usecase.blog.IsAuthorOfBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.SearchBlogPostsUseCase
import com.kanyandula.nyasa.domain.usecase.blog.UpdateBlogPostUseCase
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.state.UpdateBlogUiState
import com.kanyandula.nyasa.ui.main.blog.state.ViewBlogUiState
import com.kanyandula.nyasa.util.ErrorHandling
import com.kanyandula.nyasa.util.PreferenceKeys.BLOG_FILTER
import com.kanyandula.nyasa.util.PreferenceKeys.BLOG_ORDER
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class BlogViewModel
@Inject
constructor(
    private val searchBlogPostsUseCase: SearchBlogPostsUseCase,
    private val isAuthorOfBlogPostUseCase: IsAuthorOfBlogPostUseCase,
    private val deleteBlogPostUseCase: DeleteBlogPostUseCase,
    private val updateBlogPostUseCase: UpdateBlogPostUseCase,
    private val getBlogPostBySlugUseCase: GetBlogPostBySlugUseCase,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<BlogListUiState>(BlogListUiState()) {

    private val _viewBlogState = MutableStateFlow(ViewBlogUiState())
    val viewBlogState: StateFlow<ViewBlogUiState> = _viewBlogState.asStateFlow()

    private val _updateBlogState = MutableStateFlow(UpdateBlogUiState())
    val updateBlogState: StateFlow<UpdateBlogUiState> = _updateBlogState.asStateFlow()

    private var loadBlogJob: Job? = null
    private var authorCheckJob: Job? = null
    private var deleteJob: Job? = null
    private var updateJob: Job? = null

    private data class SearchParams(val query: String, val filterAndOrder: String)

    private val searchParams = MutableStateFlow<SearchParams?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<BlogPost>> = searchParams
        .filterNotNull()
        .flatMapLatest { params ->
            searchBlogPostsUseCase(params.query, params.filterAndOrder)
        }
        .cachedIn(viewModelScope)

    private fun updateViewBlogState(reducer: ViewBlogUiState.() -> ViewBlogUiState) {
        _viewBlogState.value = _viewBlogState.value.reducer()
    }

    private fun updateUpdateBlogState(reducer: UpdateBlogUiState.() -> UpdateBlogUiState) {
        _updateBlogState.value = _updateBlogState.value.reducer()
    }

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )
        sharedPreferences.getString(
            BLOG_ORDER,
            BlogQueryUtils.BLOG_ORDER_ASC
        )?.let {
            setBlogOrder(it)
        }
        savedStateHandle.get<String>(SAVED_SEARCH_QUERY)?.let { setQuery(it) }
        executeSearch()
    }

    companion object {
        private const val SAVED_SEARCH_QUERY = "blog_search_query"
    }

    // region Blog List

    fun getFilter(): String = viewState.value.filter

    fun getOrder(): String = viewState.value.order

    fun setQuery(query: String) {
        updateState { copy(searchQuery = query) }
        savedStateHandle[SAVED_SEARCH_QUERY] = query
    }

    fun setBlogFilter(filter: String?) {
        filter?.let { updateState { copy(filter = it) } }
    }

    fun setBlogOrder(order: String) {
        updateState { copy(order = order) }
    }

    fun executeSearch() {
        searchParams.value = SearchParams(
            viewState.value.searchQuery,
            viewState.value.order + viewState.value.filter
        )
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    // endregion

    // region View Blog

    fun isAuthorOfBlogPost(): Boolean = _viewBlogState.value.isAuthorOfBlogPost

    fun getBlogPost(): BlogPost? = _viewBlogState.value.blogPost

    fun getUpdatedBlogUri(): Uri? = _updateBlogState.value.updatedImageUri

    private fun setBlogPost(blogPost: BlogPost) {
        updateViewBlogState { copy(blogPost = blogPost) }
    }

    private fun setIsAuthorOfBlogPost(isAuthor: Boolean) {
        updateViewBlogState { copy(isAuthorOfBlogPost = isAuthor) }
    }

    fun setUpdatedBlogFields(title: String?, body: String?, uri: Uri?) {
        updateUpdateBlogState {
            copy(
                updatedBlogTitle = title ?: updatedBlogTitle,
                updatedBlogBody = body ?: updatedBlogBody,
                updatedImageUri = uri ?: updatedImageUri
            )
        }
    }

    // endregion

    // region Network Operations

    fun loadBlogBySlug(slug: String) {
        if (_viewBlogState.value.blogPost?.slug == slug) return
        loadBlogJob?.cancel()
        loadBlogJob = viewModelScope.launch {
            val blogPost = getBlogPostBySlugUseCase(slug)
            if (blogPost != null) {
                setBlogPost(blogPost)
            } else {
                sendEvent(UiEvent.ShowErrorDialog(ErrorHandling.ERROR_BLOG_POST_NOT_FOUND))
            }
        }
    }

    fun checkIsAuthorOfBlogPost(slug: String) {
        setIsAuthorOfBlogPost(false)
        authorCheckJob?.cancel()
        authorCheckJob = viewModelScope.launch {
            isAuthorOfBlogPostUseCase(slug = slug).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { isAuthor -> setIsAuthorOfBlogPost(isAuthor) }
                )
            }
        }
    }

    fun deleteBlogPost() {
        val blogPost = getBlogPost() ?: return
        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            deleteBlogPostUseCase(blogPost = blogPost).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { message ->
                        if (message == SUCCESS_BLOG_DELETED) {
                            sendEvent(UiEvent.ShowToast(message))
                            sendEvent(BlogNavigationEvent.BlogDeleted)
                        }
                    }
                )
            }
        }
    }

    fun updateBlogPost(slug: String, title: String, body: String, imageUri: Uri?) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            updateBlogPostUseCase(
                slug = slug,
                title = title,
                body = body,
                image = imageUri
            ).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { blogPost ->
                        onBlogPostUpdateSuccess(blogPost)
                        sendEvent(BlogNavigationEvent.BlogUpdateSuccess)
                    }
                )
            }
        }
    }

    // endregion

    private fun onBlogPostUpdateSuccess(blogPost: BlogPost) {
        setUpdatedBlogFields(
            uri = null,
            title = blogPost.title,
            body = blogPost.body
        )
        setBlogPost(blogPost)
    }
}
