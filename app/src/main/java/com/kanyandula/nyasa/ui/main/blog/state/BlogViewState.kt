package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.models.BlogPost


data class BlogViewState (

    // BlogFragment vars
    var blogFields: BlogFields = BlogFields(),


// ViewBlogFragment vars
    var viewBlogFields: ViewBlogFields = ViewBlogFields()


)
{
    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = ""
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlogPost: Boolean = false
    )


}