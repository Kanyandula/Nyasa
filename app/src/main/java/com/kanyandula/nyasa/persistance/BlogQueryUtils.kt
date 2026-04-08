package com.kanyandula.nyasa.persistance

import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils.ORDER_BY_ASC_DATE_UPDATED
import com.kanyandula.nyasa.persistance.BlogQueryUtils.ORDER_BY_ASC_USERNAME
import com.kanyandula.nyasa.persistance.BlogQueryUtils.ORDER_BY_DESC_DATE_UPDATED
import com.kanyandula.nyasa.persistance.BlogQueryUtils.ORDER_BY_DESC_USERNAME

object BlogQueryUtils {

    // values
    const val BLOG_ORDER_ASC: String = ""
    const val BLOG_ORDER_DESC: String = "-"
    const val BLOG_FILTER_USERNAME = "username"
    const val BLOG_FILTER_DATE_UPDATED = "date_updated"

    const val ORDER_BY_ASC_DATE_UPDATED = BLOG_ORDER_ASC + BLOG_FILTER_DATE_UPDATED
    const val ORDER_BY_DESC_DATE_UPDATED = BLOG_ORDER_DESC + BLOG_FILTER_DATE_UPDATED
    const val ORDER_BY_ASC_USERNAME = BLOG_ORDER_ASC + BLOG_FILTER_USERNAME
    const val ORDER_BY_DESC_USERNAME = BLOG_ORDER_DESC + BLOG_FILTER_USERNAME
}

fun BlogPostDao.getOrderedBlogPagingSource(
    query: String,
    filterAndOrder: String
): PagingSource<Int, BlogPost> {
    val orderByClause = when {
        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) -> "ORDER BY date_updated DESC"
        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) -> "ORDER BY date_updated ASC"
        filterAndOrder.contains(ORDER_BY_DESC_USERNAME) -> "ORDER BY username DESC"
        filterAndOrder.contains(ORDER_BY_ASC_USERNAME) -> "ORDER BY username ASC"
        else -> "ORDER BY date_updated DESC"
    }

    val sql = """
        SELECT * FROM blog_post
        WHERE title LIKE '%' || ? || '%'
        OR body LIKE '%' || ? || '%'
        OR username LIKE '%' || ? || '%'
        $orderByClause
    """.trimIndent()

    return getBlogPostsPagingSource(SimpleSQLiteQuery(sql, arrayOf(query, query, query)))
}
