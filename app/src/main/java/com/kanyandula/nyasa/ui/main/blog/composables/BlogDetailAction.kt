package com.kanyandula.nyasa.ui.main.blog.composables

sealed interface BlogDetailAction {
    data object EditClicked : BlogDetailAction
    data object DeleteClicked : BlogDetailAction
    data object NavigateBack : BlogDetailAction
    data object LikeClicked : BlogDetailAction
    data object BookmarkClicked : BlogDetailAction
    data class AuthorClicked(val username: String) : BlogDetailAction
    data class AddComment(val body: String) : BlogDetailAction
    data class DeleteComment(val commentPk: Int, val slug: String) : BlogDetailAction
}
