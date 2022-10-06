package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.kanyandula.nyasa.persistance.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED


data class BlogViewState (

    // BlogFragment vars
    var blogFields: BlogFields = BlogFields(),


// ViewBlogFragment vars
    var viewBlogFields: ViewBlogFields = ViewBlogFields()


)
{
    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = BLOG_ORDER_ASC
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlogPost: Boolean = false
    )


}