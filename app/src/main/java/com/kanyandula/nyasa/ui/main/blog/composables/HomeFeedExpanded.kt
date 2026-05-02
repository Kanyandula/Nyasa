package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.components.NyasaBlogCard
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.BlogUtils
import kotlinx.coroutines.flow.Flow

private val RAIL_WIDTH = 320.dp

@Suppress("UnusedParameter")
@Composable
internal fun HomeFeedExpanded(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit,
    onBlogClicked: (String) -> Unit,
    onVisibleSlugsChanged: (Set<String>) -> Unit = {}
) {
    val pagingItems: LazyPagingItems<BlogPost> = pagingDataFlow.collectAsLazyPagingItems()

    val visibleSlugs = pagingItems.itemSnapshotList.items.map { it.slug }.toSet()
    LaunchedEffect(visibleSlugs) {
        onVisibleSlugsChanged(visibleSlugs)
    }

    val itemCount = pagingItems.itemCount
    val hero: BlogPost? = if (itemCount > 0) pagingItems[0] else null
    val gridItems: List<BlogPost> = (1 until minOf(5, itemCount)).mapNotNull { pagingItems[it] }
    val railItems: List<BlogPost> = (5 until minOf(10, itemCount)).mapNotNull { pagingItems[it] }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.l)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(NyasaTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.l)
        ) {
            hero?.let { post ->
                item(key = "hero-${post.pk}") {
                    HeroCard(
                        blogPost = post,
                        onClick = { onBlogClicked(post.slug) }
                    )
                }
            }
            if (gridItems.isNotEmpty()) {
                val row1 = gridItems.take(2)
                val row2 = gridItems.drop(2)
                if (row1.isNotEmpty()) {
                    item(key = "grid-row-1") {
                        GridRow(
                            posts = row1,
                            onBlogClicked = onBlogClicked,
                            onAction = onAction
                        )
                    }
                }
                if (row2.isNotEmpty()) {
                    item(key = "grid-row-2") {
                        GridRow(
                            posts = row2,
                            onBlogClicked = onBlogClicked,
                            onAction = onAction
                        )
                    }
                }
            }
        }

        if (railItems.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .width(RAIL_WIDTH)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(NyasaTheme.spacing.m),
                    verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s)
                ) {
                    item(key = "rail-header") {
                        Text(
                            text = "Up next",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = NyasaTheme.spacing.s)
                        )
                    }
                    items(railItems, key = { it.pk }) { post ->
                        UpNextCard(
                            blogPost = post,
                            onClick = { onBlogClicked(post.slug) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridRow(
    posts: List<BlogPost>,
    onBlogClicked: (String) -> Unit,
    onAction: (BlogFeedAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.l)
    ) {
        posts.forEach { post ->
            NyasaBlogCard(
                title = post.title,
                authorName = post.username,
                imageUrl = post.image,
                readTime = BlogUtils.formatReadingTime(post.reading_time),
                category = post.category,
                excerpt = BlogUtils.stripHtml(post.body).take(120).takeIf { it.isNotBlank() },
                likeCount = post.like_count,
                commentCount = post.comment_count,
                onClick = { onBlogClicked(post.slug) },
                onBookmarkClick = { onAction(BlogFeedAction.BookmarkClicked(post.slug)) },
                authorAvatarUrl = post.author_avatar,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroCard(
    blogPost: BlogPost,
    onClick: () -> Unit
) {
    NyasaBlogCard(
        title = blogPost.title,
        authorName = blogPost.username,
        imageUrl = blogPost.image,
        readTime = BlogUtils.formatReadingTime(blogPost.reading_time),
        category = blogPost.category,
        excerpt = BlogUtils.stripHtml(blogPost.body).take(120).takeIf { it.isNotBlank() },
        likeCount = blogPost.like_count,
        commentCount = blogPost.comment_count,
        onClick = onClick,
        authorAvatarUrl = blogPost.author_avatar
    )
}
