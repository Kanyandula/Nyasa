package com.kanyandula.nyasa.ui.main.blog.composables

sealed interface EditBlogAction {
    data class Save(val title: String, val body: String, val tags: String) : EditBlogAction
    data object PickImage : EditBlogAction
    data object NavigateBack : EditBlogAction
    data class CategorySelected(val category: String?) : EditBlogAction
}
