@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import android.net.Uri
import com.kanyandula.nyasa.repository.main.CreateBlogRepository
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Loading
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogStateEvent
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogStateEvent.CreateNewBlogEvent
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogStateEvent.None
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState.NewBlogFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

@HiltViewModel
class CreateBlogViewModel
@Inject
constructor(
    private val createBlogRepository: CreateBlogRepository,
    @Suppress("UnusedPrivateProperty") private val sessionManager: SessionManager
) : BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {

    override fun handleStateEvent(
        stateEvent: CreateBlogStateEvent
    ): Flow<DataState<CreateBlogViewState>> {
        return when (stateEvent) {
            is CreateNewBlogEvent -> {
                val title = stateEvent.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val body = stateEvent.body.toRequestBody("text/plain".toMediaTypeOrNull())

                createBlogRepository.createNewBlogPost(
                    title,
                    body,
                    stateEvent.image
                )
            }

            is None -> {
                flowOf(
                    DataState(
                        null,
                        Loading(false),
                        null
                    )
                )
            }
        }
    }

    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let { newBlogFields.newBlogTitle = it }
        body?.let { newBlogFields.newBlogBody = it }
        uri?.let { newBlogFields.newImageUri = it }
        update.blogFields = newBlogFields
        setViewState(update)
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
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
