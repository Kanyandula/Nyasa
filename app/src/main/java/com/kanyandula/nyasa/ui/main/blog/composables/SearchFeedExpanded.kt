package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchFeedExpanded(
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

    var query by rememberSaveable { mutableStateOf(state.searchQuery) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: IllegalStateException) {
            // FocusRequester not yet attached — keyboard will open when the user taps the field
        }
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { onAction(BlogFeedAction.Search(it)) },
                onBack = { onAction(BlogFeedAction.BackClicked) },
                focusRequester = focusRequester
            )
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
                        query = query,
                        onQueryChange = { q ->
                            query = q
                            onAction(BlogFeedAction.Search(q))
                        },
                        onAction = onAction
                    )
                }

                else -> {
                    val items = pagingItems.itemSnapshotList.items

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(NyasaTheme.spacing.m),
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.l),
                        horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.l)
                    ) {
                        items(items, key = { it.pk }) { post ->
                            FeedItem(
                                blogPost = post,
                                onAction = { action ->
                                    if (action is BlogFeedAction.BlogClicked) {
                                        onBlogClicked(action.slug)
                                    } else {
                                        onAction(action)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
