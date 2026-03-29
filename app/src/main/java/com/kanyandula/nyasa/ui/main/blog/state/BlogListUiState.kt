package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_ORDER_ASC
import com.kanyandula.nyasa.persistance.BlogQueryUtils.ORDER_BY_ASC_DATE_UPDATED

data class BlogListUiState(
    val searchQuery: String = "",
    val filter: String = ORDER_BY_ASC_DATE_UPDATED,
    val order: String = BLOG_ORDER_ASC
)
