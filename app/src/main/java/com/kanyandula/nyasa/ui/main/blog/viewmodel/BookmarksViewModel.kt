package com.kanyandula.nyasa.ui.main.blog.viewmodel

import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.blog.BookmarkBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.GetBookmarksUseCase
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.main.blog.state.BookmarksUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel
@Inject
constructor(
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val bookmarkBlogPostUseCase: BookmarkBlogPostUseCase
) : BaseViewModel<BookmarksUiState>(BookmarksUiState()) {

    private var loadJob: Job? = null
    private var removeJob: Job? = null

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            getBookmarksUseCase().collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { bookmarks ->
                        updateState { copy(bookmarks = bookmarks) }
                    }
                )
            }
        }
    }

    fun removeBookmark(slug: String) {
        removeJob?.cancel()
        removeJob = viewModelScope.launch {
            bookmarkBlogPostUseCase(slug).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = {
                        updateState {
                            copy(bookmarks = bookmarks.filter { it.slug != slug })
                        }
                    }
                )
            }
        }
    }
}
