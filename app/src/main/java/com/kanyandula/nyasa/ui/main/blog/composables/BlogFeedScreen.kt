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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
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
import com.kanyandula.nyasa.ui.components.ProfileAvatar
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.BlogUtils
import com.kanyandula.nyasa.util.analytics.TrackScreen
import kotlinx.coroutines.flow.Flow

enum class FeedMode { Home, Search }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogFeedScreen(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    state: BlogListUiState,
    onAction: (BlogFeedAction) -> Unit,
    mode: FeedMode = FeedMode.Home
) {
    TrackScreen("BlogFeed")
    val pagingItems: LazyPagingItems<BlogPost> = pagingDataFlow.collectAsLazyPagingItems()
    var query by rememberSaveable { mutableStateOf(state.searchQuery) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val refreshFeed = {
        onAction(BlogFeedAction.Refresh)
        pagingItems.refresh()
    }

    LaunchedEffect(mode) {
        if (mode == FeedMode.Search) {
            searchFocusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            when (mode) {
                FeedMode.Home -> HomeTopBar(
                    onFilterClick = { showFilterSheet = true }
                )
                FeedMode.Search -> SearchTopBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { onAction(BlogFeedAction.Search(it)) },
                    onBack = { onAction(BlogFeedAction.BackClicked) },
                    focusRequester = searchFocusRequester
                )
            }
        },
        floatingActionButton = {
            if (mode == FeedMode.Home) {
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

            FeedPagingList(
                pagingItems = pagingItems,
                mode = mode,
                query = query,
                onQueryChange = { query = it },
                onRefresh = refreshFeed,
                onAction = onAction
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPagingList(
    pagingItems: LazyPagingItems<BlogPost>,
    mode: FeedMode,
    query: String,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onAction: (BlogFeedAction) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = NyasaTheme.spacing.m, vertical = NyasaTheme.spacing.s),
            verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.xl)
        ) {
            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems.peek(index)?.pk ?: index }
            ) { index ->
                pagingItems[index]?.let { blogPost ->
                    FeedItem(
                        blogPost = blogPost,
                        showEditorPick = mode == FeedMode.Home &&
                            index == 0 &&
                            query.isBlank(),
                        onAction = onAction
                    )
                }
            }
            if (pagingItems.itemCount == 0 &&
                pagingItems.loadState.refresh is LoadState.NotLoading
            ) {
                item { FeedEmptyState(query = query, onQueryChange = onQueryChange, onAction = onAction) }
            }
            if (pagingItems.itemCount > 0 &&
                pagingItems.loadState.append is LoadState.NotLoading &&
                pagingItems.loadState.append.endOfPaginationReached
            ) {
                item { EndOfFeedMessage(onRefresh = onRefresh) }
            }
            feedAppendState(appendState = pagingItems.loadState.append, onRetry = { pagingItems.retry() })
        }
    }
}

@Composable
private fun FeedItem(
    blogPost: BlogPost,
    showEditorPick: Boolean,
    onAction: (BlogFeedAction) -> Unit
) {
    if (showEditorPick) {
        EditorPickCard(
            blogPost = blogPost,
            onClick = { onAction(BlogFeedAction.BlogClicked(blogPost.slug)) }
        )
    } else {
        val excerpt = remember(blogPost.pk) {
            BlogUtils.stripHtml(blogPost.body).take(120).takeIf { it.isNotBlank() }
        }
        NyasaBlogCard(
            title = blogPost.title,
            authorName = blogPost.username,
            imageUrl = blogPost.image,
            readTime = BlogUtils.formatReadingTime(blogPost.reading_time),
            category = blogPost.category,
            excerpt = excerpt,
            likeCount = blogPost.like_count,
            commentCount = blogPost.comment_count,
            onClick = { onAction(BlogFeedAction.BlogClicked(blogPost.slug)) },
            onBookmarkClick = { onAction(BlogFeedAction.BookmarkClicked(blogPost.slug)) },
            authorAvatarUrl = blogPost.author_avatar
        )
    }
}

@Composable
private fun FeedEmptyState(
    query: String,
    onQueryChange: (String) -> Unit,
    onAction: (BlogFeedAction) -> Unit
) {
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(NyasaTheme.spacing.m))
        Text(
            text = if (query.isNotBlank()) {
                "No stories found for \"$query\""
            } else {
                "No stories yet"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (query.isNotBlank()) {
            Spacer(Modifier.height(NyasaTheme.spacing.m))
            NyasaButton(
                text = "Clear Search",
                onClick = {
                    onQueryChange("")
                    onAction(BlogFeedAction.Search(""))
                },
                style = ButtonStyle.Secondary
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.feedAppendState(
    appendState: LoadState,
    onRetry: () -> Unit
) {
    when (appendState) {
        is LoadState.Loading -> item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> item {
            NyasaButton(
                text = "Retry",
                onClick = onRetry,
                style = ButtonStyle.Secondary
            )
        }
        is LoadState.NotLoading -> Unit
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
            Column(modifier = Modifier.padding(NyasaTheme.spacing.m)) {
                Text(
                    text = "EDITOR\u2019S PICK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                Text(
                    text = blogPost.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        imageUrl = blogPost.author_avatar,
                        size = 32.dp
                    )
                    Spacer(Modifier.width(NyasaTheme.spacing.s))
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
                    Spacer(Modifier.width(NyasaTheme.spacing.xs))
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
private fun HomeTopBar(onFilterClick: () -> Unit) {
    NyasaTopBar(
        title = "NyasaBlog",
        navigationIcon = null,
        actions = {
            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit,
    focusRequester: FocusRequester
) {
    TopAppBar(
        title = {
            BlogSearchBar(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                focusRequester = focusRequester
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val chipShape = RoundedCornerShape(50)
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = NyasaTheme.spacing.m),
        horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s),
        modifier = Modifier.padding(vertical = NyasaTheme.spacing.s)
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
                shape = chipShape,
                colors = chipColors
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
                shape = chipShape,
                colors = chipColors
            )
        }
    }
}

@Composable
private fun EndOfFeedMessage(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NyasaTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Eco,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(NyasaTheme.spacing.m))
        Text(
            text = "You\u2019ve reached the roots.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(NyasaTheme.spacing.s))
        Text(
            text = "No more stories for today. Take a moment" +
                " to reflect or start your own journey.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = NyasaTheme.spacing.xl)
        )
        Spacer(Modifier.height(20.dp))
        NyasaButton(
            text = "Refresh Feed",
            onClick = onRefresh,
            style = ButtonStyle.Secondary
        )
    }
}
