package com.kanyandula.nyasa.ui.main.blog.viewmodel

import com.kanyandula.nyasa.models.BlogPost
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getFilter(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.filter
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getOrder(): String {
    getCurrentViewStateOrNew().let {
        return it.blogFields.order
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getSearchQuery(): String{
    getCurrentViewStateOrNew().let{
        return it.blogFields.searchQuery
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getPage(): Int{
    getCurrentViewStateOrNew().let{
        return it.blogFields.page
    }
}
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getIsQueryExhausted(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryExhausted
    }
}
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getIsQueryInProgress(): Boolean {
    getCurrentViewStateOrNew().let {
        return it.blogFields.isQueryInProgress
    }
}

fun BlogViewModel.getSlug(): String{
    getCurrentViewStateOrNew().let {
        it.viewBlogFields.blogPost?.let {
            return it.slug
        }
    }
    return ""
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.isAuthorOfBlogPost(): Boolean{
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.isAuthorOfBlogPost
    }
}


fun BlogViewModel.getBlogPost(): BlogPost {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost?.let {
            return it
        }?: getDummyBlogPost()
    }
}

fun BlogViewModel.getDummyBlogPost(): BlogPost{
    return BlogPost(-1, "" , "", "", "", 1, "")
}

























