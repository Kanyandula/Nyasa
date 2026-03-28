package com.kanyandula.nyasa.ui.main.blog.state

import android.net.Uri

data class UpdateBlogUiState(
    val updatedBlogTitle: String? = null,
    val updatedBlogBody: String? = null,
    val updatedImageUri: Uri? = null
)
