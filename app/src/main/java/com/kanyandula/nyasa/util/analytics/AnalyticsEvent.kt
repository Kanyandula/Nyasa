package com.kanyandula.nyasa.util.analytics

sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, String> get() = emptyMap()

    // Auth
    data object Login : AnalyticsEvent { override val name = "login" }
    data object Register : AnalyticsEvent { override val name = "register" }
    data object Logout : AnalyticsEvent { override val name = "logout" }

    // Blog engagement
    data class ViewPost(val slug: String) : AnalyticsEvent {
        override val name = "view_post"
        override val params = mapOf("slug" to slug)
    }

    data class LikePost(val slug: String) : AnalyticsEvent {
        override val name = "like_post"
        override val params = mapOf("slug" to slug)
    }

    data class BookmarkPost(val slug: String) : AnalyticsEvent {
        override val name = "bookmark_post"
        override val params = mapOf("slug" to slug)
    }

    data class CreatePost(val category: String?) : AnalyticsEvent {
        override val name = "create_post"
        override val params = category?.let { mapOf("category" to it) } ?: emptyMap()
    }

    data class DeletePost(val slug: String) : AnalyticsEvent {
        override val name = "delete_post"
        override val params = mapOf("slug" to slug)
    }

    data class CreateComment(val postSlug: String) : AnalyticsEvent {
        override val name = "create_comment"
        override val params = mapOf("post_slug" to postSlug)
    }

    data class Search(val hasQuery: Boolean) : AnalyticsEvent {
        override val name = "search"
        override val params = mapOf("has_query" to hasQuery.toString())
    }

    // Account
    data object PasswordChanged : AnalyticsEvent { override val name = "password_changed" }
    data object ProfileUpdated : AnalyticsEvent { override val name = "profile_updated" }
}
