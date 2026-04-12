package com.kanyandula.nyasa.ui.main.blog.viewmodel

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kanyandula.nyasa.domain.usecase.blog.BookmarkBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.DeleteBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.GetBlogPostBySlugUseCase
import com.kanyandula.nyasa.domain.usecase.blog.IsAuthorOfBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.LikeBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.SearchBlogPostsUseCase
import com.kanyandula.nyasa.domain.usecase.blog.UpdateBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.domain.usecase.comment.CreateCommentUseCase
import com.kanyandula.nyasa.domain.usecase.comment.DeleteCommentUseCase
import com.kanyandula.nyasa.domain.usecase.comment.GetCommentsUseCase
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.state.UpdateBlogUiState
import com.kanyandula.nyasa.ui.main.blog.state.ViewBlogUiState
import com.kanyandula.nyasa.util.BlogDetailPrefetch
import com.kanyandula.nyasa.util.BlogUtils
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
    private val likeBlogPostUseCase: LikeBlogPostUseCase,
    private val bookmarkBlogPostUseCase: BookmarkBlogPostUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
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
    private var likeJob: Job? = null
    private var bookmarkJob: Job? = null
    private var commentsJob: Job? = null
    private var addCommentJob: Job? = null
    private var deleteCommentJob: Job? = null

    private data class SearchParams(
        val query: String,
        val filterAndOrder: String,
        val category: String? = null
    )

    private val searchParams = MutableStateFlow<SearchParams?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingDataFlow: Flow<PagingData<BlogPost>> = searchParams
        .filterNotNull()
        .flatMapLatest { params ->
            searchBlogPostsUseCase(params.query, params.filterAndOrder, params.category)
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
        loadCategories()
    }

    fun setCurrentUsername(username: String) {
        updateViewBlogState { copy(currentUsername = username) }
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
            query = viewState.value.searchQuery,
            filterAndOrder = viewState.value.order + viewState.value.filter,
            category = viewState.value.selectedCategory
        )
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun setSelectedCategory(category: String?) {
        if (viewState.value.selectedCategory == category) return
        updateState { copy(selectedCategory = category) }
        executeSearch()
    }

    fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { categories ->
                        updateState { copy(categories = categories) }
                        updateUpdateBlogState { copy(categories = categories) }
                    }
                )
            }
        }
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

    fun setUpdatedBlogFields(
        title: String? = null,
        body: String? = null,
        uri: Uri? = null,
        category: String? = null,
        tags: String? = null
    ) {
        updateUpdateBlogState {
            copy(
                updatedBlogTitle = title ?: updatedBlogTitle,
                updatedBlogBody = body ?: updatedBlogBody,
                updatedImageUri = uri ?: updatedImageUri,
                updatedCategory = category ?: updatedCategory,
                updatedTags = tags ?: updatedTags
            )
        }
    }

    fun setUpdatedCategory(category: String?) {
        updateUpdateBlogState { copy(updatedCategory = category) }
    }

    fun setUpdatedTags(tags: String) {
        updateUpdateBlogState { copy(updatedTags = tags) }
    }

    // endregion

    // region Network Operations

    fun loadBlogBySlug(slug: String) {
        if (_viewBlogState.value.blogPost?.slug == slug) return

        val prefetched = BlogDetailPrefetch.pendingPost
        if (prefetched != null && prefetched.slug == slug) {
            BlogDetailPrefetch.pendingPost = null
            displayBlogPost(prefetched)
            return
        }

        loadBlogJob?.cancel()
        loadBlogJob = viewModelScope.launch {
            val blogPost = getBlogPostBySlugUseCase(slug)
            if (blogPost != null) {
                displayBlogPost(blogPost)
            } else {
                sendEvent(UiEvent.ShowErrorDialog(ErrorHandling.ERROR_BLOG_POST_NOT_FOUND))
            }
        }
    }

    private fun displayBlogPost(blogPost: BlogPost) {
        setBlogPost(blogPost)
        updateViewBlogState { copy(likeCount = blogPost.like_count ?: 0) }
        loadComments(blogPost.slug)
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
        val state = _updateBlogState.value
        val tagsList = BlogUtils.parseTags(state.updatedTags).takeIf { it.isNotEmpty() }

        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            updateBlogPostUseCase(
                slug = slug,
                title = title,
                body = body,
                image = imageUri,
                category = state.updatedCategory,
                tags = tagsList
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

    // region Like, Bookmark, Comments

    fun likeBlog(slug: String) {
        likeJob?.cancel()
        likeJob = viewModelScope.launch {
            likeBlogPostUseCase(slug).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { result ->
                        updateViewBlogState {
                            copy(isLiked = result.liked, likeCount = result.likeCount)
                        }
                    }
                )
            }
        }
    }

    fun bookmarkBlog(slug: String) {
        bookmarkJob?.cancel()
        bookmarkJob = viewModelScope.launch {
            bookmarkBlogPostUseCase(slug).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { bookmarked ->
                        updateViewBlogState { copy(isBookmarked = bookmarked) }
                    }
                )
            }
        }
    }

    fun loadComments(slug: String) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            getCommentsUseCase(slug).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { comments ->
                        updateViewBlogState { copy(comments = comments) }
                    }
                )
            }
        }
    }

    fun addComment(slug: String, body: String) {
        addCommentJob?.cancel()
        addCommentJob = viewModelScope.launch {
            createCommentUseCase(slug, body).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { comment ->
                        updateViewBlogState {
                            copy(comments = comments + comment)
                        }
                    }
                )
            }
        }
    }

    fun deleteComment(pk: Int) {
        deleteCommentJob?.cancel()
        deleteCommentJob = viewModelScope.launch {
            deleteCommentUseCase(pk).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = {
                        updateViewBlogState {
                            copy(comments = comments.filter { it.pk != pk })
                        }
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
