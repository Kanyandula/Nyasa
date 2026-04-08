package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.ui.components.ButtonStyle
import com.kanyandula.nyasa.ui.components.NyasaBlogCard
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.util.BlogUtils
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogFeedScreen(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit
) {
    val pagingItems: LazyPagingItems<BlogPost> = pagingDataFlow.collectAsLazyPagingItems()
    var query by rememberSaveable { mutableStateOf(state.searchQuery) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    val refreshFeed = {
        onAction(BlogFeedAction.Refresh)
        pagingItems.refresh()
    }

    Scaffold(
        topBar = {
            NyasaTopBar(
                title = "NyasaBlog",
                navigationIcon = null,
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showSearch) {
                BlogSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { onAction(BlogFeedAction.Search(it)) },
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )
            }

            // Category chips
            if (state.categories.isNotEmpty()) {
                CategoryChipsRow(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { onAction(BlogFeedAction.CategorySelected(it)) }
                )
            }

            PullToRefreshBox(
                isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
                onRefresh = refreshFeed,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = { index ->
                            pagingItems.peek(index)?.pk ?: index
                        }
                    ) { index ->
                        pagingItems[index]?.let { blogPost ->
                            if (index == 0 && query.isBlank()) {
                                EditorPickCard(
                                    blogPost = blogPost,
                                    onClick = {
                                        onAction(BlogFeedAction.BlogClicked(blogPost.slug))
                                    }
                                )
                            } else {
                                NyasaBlogCard(
                                    title = blogPost.title,
                                    authorName = blogPost.username,
                                    imageUrl = blogPost.image,
                                    readTime = BlogUtils
                                        .formatReadingTime(
                                            blogPost.reading_time
                                        ),
                                    category = blogPost.category,
                                    excerpt = blogPost.body
                                        .take(120)
                                        .takeIf { it.isNotBlank() },
                                    likeCount = blogPost.like_count,
                                    onClick = {
                                        onAction(BlogFeedAction.BlogClicked(blogPost.slug))
                                    },
                                    onBookmarkClick = {
                                        onAction(BlogFeedAction.BookmarkClicked(blogPost.slug))
                                    }
                                )
                            }
                        }
                    }

                    // Empty state
                    if (pagingItems.itemCount == 0 &&
                        pagingItems.loadState.refresh is LoadState.NotLoading
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme
                                        .onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (query.isNotBlank()) {
                                        "No stories found for \"$query\""
                                    } else {
                                        "No stories yet"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant
                                )
                                if (query.isNotBlank()) {
                                    Spacer(Modifier.height(16.dp))
                                    NyasaButton(
                                        text = "Clear Search",
                                        onClick = {
                                            query = ""
                                            onAction(BlogFeedAction.Search(""))
                                        },
                                        style = ButtonStyle.Secondary
                                    )
                                }
                            }
                        }
                    }

                    // End-of-list message
                    if (pagingItems.itemCount > 0 &&
                        pagingItems.loadState.append is LoadState.NotLoading &&
                        pagingItems.loadState.append.endOfPaginationReached
                    ) {
                        item {
                            EndOfFeedMessage(onRefresh = refreshFeed)
                        }
                    }

                    when (pagingItems.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                NyasaButton(
                                    text = "Retry",
                                    onClick = { pagingItems.retry() },
                                    style = ButtonStyle.Secondary
                                )
                            }
                        }
                        is LoadState.NotLoading -> { /* no-op */ }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        BlogFilterSheet(
            currentFilter = state.filter,
            currentOrder = state.order,
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            onApply = { filter, order ->
                onAction(BlogFeedAction.FilterApply(filter, order))
                showFilterSheet = false
            },
            onCategorySelected = { category ->
                onAction(BlogFeedAction.CategorySelected(category))
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
private fun EditorPickCard(
    blogPost: BlogPost,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            if (blogPost.image.isNotBlank()) {
                AsyncImage(
                    model = blogPost.image,
                    contentDescription = blogPost.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "EDITOR\u2019S PICK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = blogPost.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Author avatar placeholder
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = blogPost.username
                                    .take(2).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme
                                    .onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = blogPost.username,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onClick)
                ) {
                    Text(
                        text = "Read More",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        items(categories, key = { it.pk }) { category ->
            FilterChip(
                selected = selectedCategory == category.slug,
                onClick = { onCategorySelected(category.slug) },
                label = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun EndOfFeedMessage(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "You\u2019ve reached the roots.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No more stories for today. Take a moment" +
                " to reflect or start your own journey.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(20.dp))
        NyasaButton(
            text = "Refresh Feed",
            onClick = onRefresh,
            style = ButtonStyle.Secondary
        )
    }
}
