package com.kanyandula.nyasa.ui.main.blog.state

import android.net.Uri
import com.kanyandula.nyasa.models.Category

data class UpdateBlogUiState(
    val updatedBlogTitle: String? = null,
    val updatedImageUri: Uri? = null,
    val originalImageUrl: String? = null,
    val updatedCategory: String? = null,
    val updatedTags: String? = null,
    val categories: List<Category> = emptyList()
)
