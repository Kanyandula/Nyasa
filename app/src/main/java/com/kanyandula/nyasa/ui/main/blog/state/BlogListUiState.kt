package com.kanyandula.nyasa.ui.main.blog.state

import android.os.Parcelable
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_ORDER_ASC
import com.kanyandula.nyasa.persistance.BlogQueryUtils.ORDER_BY_ASC_DATE_UPDATED

data class BlogListUiState(
    val blogList: List<BlogPost> = emptyList(),
    val searchQuery: String = "",
    val page: Int = 1,
    val isQueryInProgress: Boolean = false,
    val isQueryExhausted: Boolean = false,
    val filter: String = ORDER_BY_ASC_DATE_UPDATED,
    val order: String = BLOG_ORDER_ASC,
    val layoutManagerState: Parcelable? = null
)
