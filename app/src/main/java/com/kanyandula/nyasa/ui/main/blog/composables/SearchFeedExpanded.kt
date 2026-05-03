package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import kotlinx.coroutines.flow.Flow

@Suppress("UnusedParameter") // state passed for symmetry with BlogFeedScreen; reserved for future empty/error states
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
                showEditorPick = false,
                onAction = { action ->
                    if (action is BlogFeedAction.BlogClicked) onBlogClicked(action.slug) else onAction(action)
                }
            )
        }
    }
}
