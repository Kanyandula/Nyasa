package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.Comment

data class ViewBlogUiState(
    val blogPost: BlogPost? = null,
    val isAuthorOfBlogPost: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val likeCount: Int = 0,
    val currentUsername: String = ""
)
