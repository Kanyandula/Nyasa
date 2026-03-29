package com.kanyandula.nyasa.ui.navigation

object Routes {
    const val BLOG_GRAPH = "blog"
    const val BLOG_FEED = "blog/feed"
    const val BLOG_DETAIL = "blog/detail/{slug}"
    fun blogDetail(slug: String) = "blog/detail/$slug"
    const val BLOG_EDIT = "blog/edit/{slug}"
    fun blogEdit(slug: String) = "blog/edit/$slug"

    const val CREATE = "create"

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
