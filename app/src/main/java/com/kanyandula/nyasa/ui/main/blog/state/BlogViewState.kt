package com.kanyandula.nyasa.ui.main.blog.state

import android.net.Uri
import android.os.Parcelable
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.kanyandula.nyasa.persistance.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlogViewState (

    // BlogFragment vars
    var blogFields: BlogFields = BlogFields(),


// ViewBlogFragment vars
    var viewBlogFields: ViewBlogFields = ViewBlogFields(),

    // UpdateBlogFragment vars
    var updatedBlogFields: UpdatedBlogFields = UpdatedBlogFields(),

    var accountFields: AccountFields = AccountFields(),


) : Parcelable {
    @Parcelize
    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = BLOG_ORDER_ASC
    ) : Parcelable

    @Parcelize
    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlogPost: Boolean = false
    ) : Parcelable

    @Parcelize
    data class UpdatedBlogFields(
        var updatedBlogTitle: String? = null,
        var updatedBlogBody: String? = null,
        var updatedImageUri: Uri? = null
    ) : Parcelable

    @Parcelize
    data class AccountFields(
        var accountProperties: AccountProperties? = null

    ): Parcelable


}