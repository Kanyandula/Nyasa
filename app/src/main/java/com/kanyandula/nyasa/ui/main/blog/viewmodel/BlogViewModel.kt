package com.kanyandula.nyasa.ui.main.blog.viewmodel

import android.content.SharedPreferences
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var searchJob: Job? = null
    private var loadBlogJob: Job? = null
    private var authorCheckJob: Job? = null
    private var deleteJob: Job? = null
    private var updateJob: Job? = null

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
    }

    companion object {
        private const val SAVED_SEARCH_QUERY = "blog_search_query"
    }

    // region Blog List (Getters)

    fun getFilter(): String = viewState.value.filter

    fun getOrder(): String = viewState.value.order

    fun getSearchQuery(): String = viewState.value.searchQuery

    fun getPage(): Int = viewState.value.page

    fun getIsQueryExhausted(): Boolean = viewState.value.isQueryExhausted

    fun getIsQueryInProgress(): Boolean = viewState.value.isQueryInProgress

    // endregion

    // region View Blog (Getters)

    fun isAuthorOfBlogPost(): Boolean = _viewBlogState.value.isAuthorOfBlogPost

    fun getBlogPost(): BlogPost? = _viewBlogState.value.blogPost

    fun getUpdatedBlogUri(): Uri? = _updateBlogState.value.updatedImageUri

    // endregion

    // region Blog List (Setters)

    fun setLayoutManagerState(state: Parcelable) {
        updateState { copy(layoutManagerState = state) }
    }

    fun setQuery(query: String) {
        updateState { copy(searchQuery = query) }
        savedStateHandle[SAVED_SEARCH_QUERY] = query
    }

    fun setBlogListData(blogList: List<BlogPost>) {
        updateState { copy(blogList = blogList) }
    }

    fun setQueryExhausted(isExhausted: Boolean) {
        updateState { copy(isQueryExhausted = isExhausted) }
    }

    fun setQueryInProgress(isInProgress: Boolean) {
        updateState { copy(isQueryInProgress = isInProgress) }
    }

    fun setBlogFilter(filter: String?) {
        filter?.let { updateState { copy(filter = it) } }
    }

    fun setBlogOrder(order: String) {
        updateState { copy(order = order) }
    }

    // endregion

    // region View Blog (Setters)

    private fun setBlogPost(blogPost: BlogPost) {
        updateViewBlogState { copy(blogPost = blogPost) }
    }

    private fun setIsAuthorOfBlogPost(isAuthor: Boolean) {
        updateViewBlogState { copy(isAuthorOfBlogPost = isAuthor) }
    }

    fun removeDeletedBlogPost() {
        val target = getBlogPost() ?: return
        val list = viewState.value.blogList.toMutableList()
        list.remove(target)
        setBlogListData(list)
    }

    // endregion

    // region Update Blog (Setters)

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

    // region Pagination

    fun loadFirstPage() {
        updateState { copy(isQueryInProgress = true, isQueryExhausted = false, page = 1) }
        searchBlogPosts()
        Log.d(TAG, "BlogViewModel: loadFirstPage: ${getSearchQuery()}")
    }

    private fun incrementPageNumber() {
        updateState { copy(page = page + 1) }
    }

    fun nextPage() {
        if (!getIsQueryInProgress() && !getIsQueryExhausted()) {
            Log.d(TAG, "BlogViewModel: Attempting to load next page...")
            incrementPageNumber()
            setQueryInProgress(true)
            searchBlogPosts()
        }
    }

    // endregion

    // region Network Operations

    private fun searchBlogPosts() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            searchBlogPostsUseCase(
                query = getSearchQuery(),
                filterAndOrder = getOrder() + getFilter(),
                page = getPage()
            ).collect { resource ->
                handleResource(
                    resource,
                    onLoading = { data ->
                        data?.let { handleIncomingBlogListData(it.blogList, it.isQueryExhausted) }
                    },
                    onSuccess = { data ->
                        handleIncomingBlogListData(data.blogList, data.isQueryExhausted)
                    },
                    onError = { message ->
                        setLoading(false)
                        if (ErrorHandling.isPaginationDone(message)) {
                            setQueryExhausted(true)
                            setQueryInProgress(false)
                        } else {
                            handleError(message)
                        }
                    }
                )
            }
        }
    }

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
                            removeDeletedBlogPost()
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

    private fun handleIncomingBlogListData(
        blogList: List<BlogPost>,
        isQueryExhausted: Boolean
    ) {
        updateState {
            copy(
                isQueryInProgress = false,
                isQueryExhausted = isQueryExhausted,
                blogList = blogList
            )
        }
    }

    private fun onBlogPostUpdateSuccess(blogPost: BlogPost) {
        setUpdatedBlogFields(
            uri = null,
            title = blogPost.title,
            body = blogPost.body
        )
        setBlogPost(blogPost)
        updateListItem(blogPost)
    }

    private fun updateListItem(newBlogPost: BlogPost) {
        val list = viewState.value.blogList.toMutableList()
        for (i in list.indices) {
            if (list[i].pk == newBlogPost.pk) {
                list[i] = newBlogPost
                break
            }
        }
        setBlogListData(list)
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }
}
