package com.kanyandula.nyasa.ui.main.blog.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.kanyandula.nyasa.persistance.BlogQueryUtils
import com.kanyandula.nyasa.repository.main.BlogRepository
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Loading
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.BlogSearchEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.CheckAuthorOfBlogPost
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.DeleteBlogPostEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.None
import com.kanyandula.nyasa.ui.main.blog.state.BlogStateEvent.UpdateBlogPostEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogViewState
import com.kanyandula.nyasa.util.PreferenceKeys.BLOG_FILTER
import com.kanyandula.nyasa.util.PreferenceKeys.BLOG_ORDER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class BlogViewModel
@Inject
constructor(
    @Suppress("UnusedPrivateMember") private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor

) : BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )
        sharedPreferences.getString(
            BLOG_ORDER,
            BlogQueryUtils.BLOG_ORDER_ASC
        )?.let {
            setBlogOrder(
                it
            )
        }
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        return when (stateEvent) {
            is BlogSearchEvent -> {
                return blogRepository.searchBlogPosts(
                    query = getSearchQuery(),
                    filterAndOrder = getOrder() + getFilter(),
                    page = getPage()
                )
            }

            is CheckAuthorOfBlogPost -> {
                return blogRepository.isAuthorOfBlogPost(
                    slug = getSlug()
                )
            }

            is DeleteBlogPostEvent -> {
                return blogRepository.deleteBlogPost(
                    blogPost = getBlogPost()
                )
            }

            is UpdateBlogPostEvent -> {
                val title = RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    stateEvent.title
                )
                val body = RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    stateEvent.body
                )

                return blogRepository.updateBlogPost(
                    slug = getSlug(),
                    title = title,
                    body = body,
                    image = stateEvent.image
                )
            }

            is None -> {
                return object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}
