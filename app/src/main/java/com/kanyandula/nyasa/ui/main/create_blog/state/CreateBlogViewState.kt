@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.state

import android.net.Uri

data class CreateBlogViewState(
    val blogFields: NewBlogFields = NewBlogFields()
) {
    data class NewBlogFields(
        val newBlogTitle: String? = null,
        val newBlogBody: String? = null,
        val newImageUri: Uri? = null
    )
}
