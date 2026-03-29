package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.components.ButtonStyle
import com.kanyandula.nyasa.ui.components.NyasaBlogCard
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.util.DateUtils
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogFeedScreen(
    pagingDataFlow: Flow<PagingData<BlogPost>>,
    searchQuery: String,
    currentFilter: String,
    currentOrder: String,
    onBlogClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFilterApply: (filter: String, order: String) -> Unit,
    onRefresh: () -> Unit
) {
    val pagingItems: LazyPagingItems<BlogPost> = pagingDataFlow.collectAsLazyPagingItems()
    var query by rememberSaveable { mutableStateOf(searchQuery) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NyasaTopBar(
                title = "NyasaBlog",
                navigationIcon = null,
                actions = {
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
            BlogSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { onSearch(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            PullToRefreshBox(
                isRefreshing = pagingItems.loadState.refresh is LoadState.Loading,
                onRefresh = {
                    onRefresh()
                    pagingItems.refresh()
                },
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
                        key = { index -> pagingItems.peek(index)?.pk ?: index }
                    ) { index ->
                        pagingItems[index]?.let { blogPost ->
                            NyasaBlogCard(
                                title = blogPost.title,
                                authorName = blogPost.username,
                                imageUrl = blogPost.image,
                                readTime = DateUtils.convertLongToStringDate(blogPost.date_updated),
                                onClick = { onBlogClick(blogPost.slug) }
                            )
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
            currentFilter = currentFilter,
            currentOrder = currentOrder,
            onApply = { filter, order ->
                onFilterApply(filter, order)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}
