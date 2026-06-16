package com.kanyandula.nyasa.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (navigation-compose 2.8+ `@Serializable` keys).
 *
 * Graph markers — [AuthGraph], [MainGraph], [BlogGraph], [AccountGraph] — are nested-graph
 * wrappers used with `navigation<T>` / `getBackStackEntry<T>`; everything else is a leaf screen
 * registered with `composable<T>`. Screens that carry arguments are `data class`es; argument-free
 * screens are `object`s.
 */
object Routes {
    // Graph wrappers
    @Serializable
    object AuthGraph

    @Serializable
    object MainGraph

    @Serializable
    object BlogGraph

    @Serializable
    object AccountGraph

    // Blog
    @Serializable
    object BlogFeed

    @Serializable
    object BlogSearch

    @Serializable
    data class BlogDetail(val slug: String)

    @Serializable
    data class BlogEdit(val slug: String)

    @Serializable
    object Create

    @Serializable
    data class AuthorProfile(val username: String)

    @Serializable
    object Bookmarks

    // Account
    @Serializable
    object AccountProfile

    @Serializable
    object AccountEdit

    @Serializable
    object AccountChangePassword

    // Auth
    @Serializable
    object Welcome

    @Serializable
    object Login

    @Serializable
    object Register

    @Serializable
    object ForgotPassword
}
