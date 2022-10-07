package com.kanyandula.nyasa.ui.main.blog.state

import okhttp3.MultipartBody

sealed class BlogStateEvent {

    class BlogSearchEvent : BlogStateEvent()

    class CheckAuthorOfBlogPost: BlogStateEvent()

    class DeleteBlogPostEvent: BlogStateEvent()

    data class UpdateBlogPostEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part?
    ): BlogStateEvent()

    class None: BlogStateEvent()
}