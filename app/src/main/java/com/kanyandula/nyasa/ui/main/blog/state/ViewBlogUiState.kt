package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.models.BlogPost

data class ViewBlogUiState(
    val blogPost: BlogPost? = null,
    val isAuthorOfBlogPost: Boolean = false
)
