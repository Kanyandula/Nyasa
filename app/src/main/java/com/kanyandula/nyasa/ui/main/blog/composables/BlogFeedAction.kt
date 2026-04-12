package com.kanyandula.nyasa.ui.main.blog.composables

sealed interface BlogFeedAction {
    data class BlogClicked(val slug: String) : BlogFeedAction
    data class Search(val query: String) : BlogFeedAction
    data class FilterApply(val filter: String, val order: String) : BlogFeedAction
    data class CategorySelected(val category: String?) : BlogFeedAction
    data class BookmarkClicked(val slug: String) : BlogFeedAction
    data object CreateClicked : BlogFeedAction
    data object BackClicked : BlogFeedAction
    data object Refresh : BlogFeedAction
}
