package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.models.BlogPost

data class BookmarksUiState(
    val bookmarks: List<BlogPost> = emptyList()
)
