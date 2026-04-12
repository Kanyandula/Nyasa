package com.kanyandula.nyasa.ui.navigation

import android.net.Uri

object Routes {
    const val AUTH_GRAPH = "auth"
    const val MAIN_GRAPH = "main"

    const val BLOG_GRAPH = "blog"
    const val BLOG_FEED = "blog/feed"
    const val BLOG_SEARCH = "blog/search"
    const val BLOG_DETAIL = "blog/detail/{slug}"
    fun blogDetail(slug: String) = "blog/detail/${Uri.encode(slug)}"
    const val BLOG_EDIT = "blog/edit/{slug}"
    fun blogEdit(slug: String) = "blog/edit/${Uri.encode(slug)}"

    const val CREATE = "create"

    const val AUTHOR_PROFILE = "author/{username}"
    fun authorProfile(username: String) = "author/${Uri.encode(username)}"

    const val BOOKMARKS = "bookmarks"

    const val ACCOUNT_GRAPH = "account"
    const val ACCOUNT_PROFILE = "account/profile"
    const val ACCOUNT_EDIT = "account/edit"
    const val ACCOUNT_CHANGE_PASSWORD = "account/change-password"

    // Auth
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot-password"
}
