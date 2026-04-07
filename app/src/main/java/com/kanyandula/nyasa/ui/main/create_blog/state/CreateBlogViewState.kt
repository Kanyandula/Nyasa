@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.state

import android.net.Uri
import com.kanyandula.nyasa.models.Category

data class CreateBlogViewState(
    val blogFields: NewBlogFields = NewBlogFields(),
    val categories: List<Category> = emptyList()
) {
    data class NewBlogFields(
        val newBlogTitle: String? = null,
        val newBlogBody: String? = null,
        val newImageUri: Uri? = null,
        val category: String? = null,
        val tags: String? = null
    )
}
