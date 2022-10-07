package com.kanyandula.nyasa.ui.main.blog.viewmodel


import android.net.Uri
import com.kanyandula.nyasa.models.BlogPost
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setQuery(query: String){
    val update = getCurrentViewStateOrNew()
    update.blogFields.searchQuery = query
    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setBlogListData(blogList: List<BlogPost>){
    val update = getCurrentViewStateOrNew()
    update.blogFields.blogList = blogList
    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setBlogPost(blogPost: BlogPost){
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.blogPost = blogPost
    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setIsAuthorOfBlogPost(isAuthorOfBlogPost: Boolean){
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.isAuthorOfBlogPost = isAuthorOfBlogPost
    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setQueryExhausted(isExhausted: Boolean){
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryExhausted = isExhausted
    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setQueryInProgress(isInProgress: Boolean){
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryInProgress = isInProgress
    setViewState(update)

}


// Filter can be "date_updated" or "username"
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setBlogFilter(filter: String?){
    filter?.let{
        val update = getCurrentViewStateOrNew()
        update.blogFields.filter = filter
        setViewState(update)
    }
}

// Order can be "-" or ""
// Note: "-" = DESC, "" = ASC
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setBlogOrder(order: String){
    val update = getCurrentViewStateOrNew()
    update.blogFields.order = order
    setViewState(update)
}


@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.removeDeletedBlogPost(){
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for(i in 0..(list.size - 1)){
        if(list[i] == getBlogPost()){
            list.remove(getBlogPost())
            break
        }
    }
    setBlogListData(list)
//    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.setUpdatedBlogFields(title: String?, body: String?, uri: Uri?){
    val update = getCurrentViewStateOrNew()
    val updatedBlogFields = update.updatedBlogFields
    title?.let{ updatedBlogFields.updatedBlogTitle = it }
    body?.let{ updatedBlogFields.updatedBlogBody = it }
    uri?.let{ updatedBlogFields.updatedImageUri = it }
    update.updatedBlogFields = updatedBlogFields
    setViewState(update)
}

@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.updateListItem(newBlogPost: BlogPost){
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for(i in 0..(list.size - 1)){
        if(list[i].pk == newBlogPost.pk){
            list[i] = newBlogPost
            break
        }
    }
    update.blogFields.blogList = list
    setViewState(update)
}


@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.onBlogPostUpdateSuccess(blogPost: BlogPost){
    setUpdatedBlogFields(
        uri = null,
        title = blogPost.title,
        body = blogPost.body
    ) // update UpdateBlogFragment (not really necessary since navigating back)
    setBlogPost(blogPost) // update ViewBlogFragment
    updateListItem(blogPost) // update BlogFragment
}




