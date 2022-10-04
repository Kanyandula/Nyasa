package com.kanyandula.nyasa.ui.main.blog

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.repository.main.BlogRepository
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.*
import com.kanyandula.nyasa.ui.main.blog.state.BlogViewState
import com.kanyandula.nyasa.util.AbsentLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltViewModel
class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val requestManager: RequestManager
): BaseViewModel<BlogStateEvent, BlogViewState>(){
    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent){

            is BlogSearchEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        viewState.value!!.blogFields.searchQuery
                    )
                }?: AbsentLiveData.create()
            }

            is CheckAuthorOfBlogPost -> {
                return AbsentLiveData.create()
            }

            is None ->{
                return AbsentLiveData.create()
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun setQuery(query: String){
        val update = getCurrentViewStateOrNew()
        update.blogFields.searchQuery = query
        _viewState.value = update
    }

    fun setBlogListData(blogList: List<BlogPost>){
        val update = getCurrentViewStateOrNew()
        update.blogFields.blogList = blogList
        _viewState.value = update
    }

    fun setBlogPost(blogPost: BlogPost){
        val update = getCurrentViewStateOrNew()
        update.viewBlogFields.blogPost = blogPost
        _viewState.value = update
    }

    fun setIsAuthorOfBlogPost(isAuthorOfBlogPost: Boolean){
        val update = getCurrentViewStateOrNew()
        update.viewBlogFields.isAuthorOfBlogPost = isAuthorOfBlogPost
        _viewState.value = update
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}



