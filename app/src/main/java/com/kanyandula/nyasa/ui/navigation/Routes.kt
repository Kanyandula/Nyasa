package com.kanyandula.nyasa.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (navigation-compose 2.8+ `@Serializable` keys).
 *
 * Graph markers — [AuthGraph], [MainGraph], [AccountGraph] — are nested-graph wrappers used with
 * `navigation<T>` / `getBackStackEntry<T>`; everything else is a leaf screen
 * registered with `composable<T>`. Screens that carry arguments are `data class`es; argument-free
 * screens are `object`s.
 *
 * [TabRoute] marks the leaf destinations a bottom-bar tab may target, so the
 * "a tab points at a leaf, never a graph wrapper" invariant is compiler-checked via
 * `MainNavItem.route` rather than only test-checked.
 */
object Routes {
    /**
     * A leaf route that [com.kanyandula.nyasa.ui.navigation.MainNavItem.route] directly navigates to
     * on a tab tap. Subsidiary screens reached within a tab (e.g. [BlogDetail], [BlogEdit]) are not
     * `TabRoute`s even though they highlight the same tab.
     */
    sealed interface TabRoute

    // Graph wrappers
    @Serializable
    object AuthGraph

    @Serializable
    object MainGraph

    @Serializable
    object AccountGraph

    // Blog
    @Serializable
    object BlogFeed : TabRoute

    @Serializable
    object BlogSearch : TabRoute

    @Serializable
    data class BlogDetail(val slug: String)

    @Serializable
    data class BlogEdit(val slug: String)

    @Serializable
    object Create

    @Serializable
    data class AuthorProfile(val username: String)

    @Serializable
    object Bookmarks : TabRoute

    // Account
    @Serializable
    object AccountProfile : TabRoute

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
