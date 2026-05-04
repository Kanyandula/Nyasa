package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            HomeTopBar(onFilterClick = { /* filter sheet is compact-only; no-op on expanded */ })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(BlogFeedAction.CreateClicked) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Create post"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.categories.isNotEmpty()) {
                CategoryChipsRow(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { onAction(BlogFeedAction.CategorySelected(it)) }
                )
            }

            val itemCount = pagingItems.itemCount
            val isRefreshing = pagingItems.loadState.refresh is LoadState.Loading
            val isEmpty = itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading

            when {
                isRefreshing && itemCount == 0 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isEmpty -> {
                    FeedEmptyState(
                        query = state.searchQuery,
                        onQueryChange = { onAction(BlogFeedAction.Search(it)) },
                        onAction = onAction
                    )
                }

                else -> {
                    HeroAndGrid(
                        pagingItems = pagingItems,
                        onBlogClicked = onBlogClicked,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroAndGrid(
    pagingItems: LazyPagingItems<BlogPost>,
    onBlogClicked: (String) -> Unit,
    onAction: (BlogFeedAction) -> Unit
) {
    val items = pagingItems.itemSnapshotList.items
    val hero: BlogPost? = items.getOrNull(0)
    val gridItems: List<BlogPost> = items.drop(1).take(4)
    val railItems: List<BlogPost> = items.drop(5).take(5)

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
                    EditorPickCard(
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
                excerpt = remember(post.pk) {
                    BlogUtils.stripHtml(post.body).take(120).takeIf { it.isNotBlank() }
                },
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
