package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.ui.components.ButtonStyle
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.components.ProfileAvatar
import com.kanyandula.nyasa.ui.components.htmlrenderer.PostBodyRenderer
import com.kanyandula.nyasa.ui.main.blog.state.ViewBlogUiState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.BlogUtils
import com.kanyandula.nyasa.util.DateUtils
import com.kanyandula.nyasa.util.analytics.TrackScreen
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlogDetailScreen(
    state: ViewBlogUiState,
    isLoading: Boolean,
    onAction: (BlogDetailAction) -> Unit
) {
    TrackScreen("BlogDetail")
    val blogPost = state.blogPost
    val isLiked = state.isLiked
    val isBookmarked = state.isBookmarked

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "",
                    onNavigationClick = { onAction(BlogDetailAction.NavigateBack) },
                    actions = {
                        IconButton(onClick = { onAction(BlogDetailAction.LikeClicked) }) {
                            Icon(
                                imageVector = if (isLiked) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = "Like",
                                tint = if (isLiked) {
                                    NyasaTheme.colors.likeActive
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        IconButton(onClick = { onAction(BlogDetailAction.BookmarkClicked) }) {
                            Icon(
                                imageVector = if (isBookmarked) {
                                    Icons.Filled.Bookmark
                                } else {
                                    Icons.Outlined.BookmarkBorder
                                },
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (blogPost != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                ) {
                    Box {
                        AsyncImage(
                            model = blogPost.image,
                            contentDescription = blogPost.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Crop
                        )
                        val category = blogPost.category
                        if (!category.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopStart),
                                color = NyasaTheme.colors.categoryChip
                                    .copy(alpha = 0.85f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = category.uppercase(),
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    ),
                                    style = MaterialTheme.typography
                                        .labelSmall.copy(
                                            letterSpacing = 1.sp
                                        ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(
                            horizontal = NyasaTheme.spacing.l,
                            vertical = 20.dp
                        )
                    ) {
                        Text(
                            text = blogPost.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(NyasaTheme.spacing.m))

                        AuthorRow(
                            username = blogPost.username,
                            date = DateUtils.convertLongToStringDate(
                                blogPost.date_updated
                            ),
                            readingTime = blogPost.reading_time,
                            likeCount = state.likeCount,
                            onAuthorClick = {
                                onAction(BlogDetailAction.AuthorClicked(blogPost.username))
                            },
                            authorAvatarUrl = blogPost.author_avatar
                        )

                        Spacer(Modifier.height(NyasaTheme.spacing.l))

                        PostBodyRenderer(html = blogPost.body)

                        val parsedTags = remember(blogPost.tags) {
                            BlogUtils.parseTags(blogPost.tags)
                        }
                        if (parsedTags.isNotEmpty()) {
                            Spacer(Modifier.height(NyasaTheme.spacing.l))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme
                                    .surfaceContainerHigh
                            )
                            Spacer(Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement
                                    .spacedBy(NyasaTheme.spacing.s),
                                verticalArrangement = Arrangement
                                    .spacedBy(NyasaTheme.spacing.xs)
                            ) {
                                parsedTags.forEach { tag ->
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography
                                            .labelMedium,
                                        color = MaterialTheme.colorScheme
                                            .primary
                                    )
                                }
                            }
                        }

                        if (state.isAuthorOfBlogPost) {
                            Spacer(Modifier.height(NyasaTheme.spacing.xl))
                            NyasaButton(
                                text = "Edit",
                                onClick = { onAction(BlogDetailAction.EditClicked) },
                                style = ButtonStyle.Secondary,
                                trailingIcon = Icons.Filled.EditNote
                            )
                            Spacer(Modifier.height(12.dp))
                            NyasaButton(
                                text = "Delete",
                                onClick = { onAction(BlogDetailAction.DeleteClicked) },
                                style = ButtonStyle.Destructive,
                                trailingIcon = Icons.Filled.Delete
                            )
                        }

                        Spacer(Modifier.height(NyasaTheme.spacing.xl))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme
                                .surfaceContainerHigh
                        )
                        Spacer(Modifier.height(NyasaTheme.spacing.m))

                        CommentsSection(
                            comments = state.comments,
                            currentUsername = state.currentUsername,
                            onAddComment = { onAction(BlogDetailAction.AddComment(it)) },
                            onDeleteComment = {
                                onAction(
                                    BlogDetailAction.DeleteComment(it, blogPost.slug)
                                )
                            }
                        )
                    }
                }
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun AuthorRow(
    username: String,
    date: String,
    readingTime: Int?,
    likeCount: Int,
    onAuthorClick: () -> Unit,
    authorAvatarUrl: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(imageUrl = authorAvatarUrl, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = username,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onAuthorClick)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (readingTime != null) {
                    Text(
                        text = "\u00B7",
                        style = MaterialTheme.typography.bodySmall,
                        color = NyasaTheme.colors.readTimeText
                    )
                    Text(
                        text = "$readingTime min read",
                        style = MaterialTheme.typography.bodySmall,
                        color = NyasaTheme.colors.readTimeText
                    )
                }
            }
        }
        if (likeCount > 0) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(NyasaTheme.spacing.xs))
            Text(
                text = "$likeCount",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommentsSection(
    comments: List<Comment>,
    currentUsername: String,
    onAddComment: (String) -> Unit,
    onDeleteComment: (Int) -> Unit
) {
    var commentText by rememberSaveable { mutableStateOf("") }

    Text(
        text = "Comments (${comments.size})",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NyasaTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = "Add a comment...",
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(Modifier.width(NyasaTheme.spacing.s))
        IconButton(
            onClick = {
                if (commentText.isNotBlank()) {
                    onAddComment(commentText)
                    commentText = ""
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send comment",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    Spacer(Modifier.height(NyasaTheme.spacing.m))
    comments.forEach { comment ->
        val canDelete = comment.username == currentUsername
        CommentItem(
            comment = comment,
            onDelete = if (canDelete) {
                { onDeleteComment(comment.pk) }
            } else {
                null
            }
        )
        Spacer(Modifier.height(NyasaTheme.spacing.s))
    }

    if (comments.isEmpty()) {
        Text(
            text = "No comments yet. Be the first to share your thoughts!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onDelete: (() -> Unit)?
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateUtils.convertLongToStringDate(
                            comment.dateCreated
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete comment",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(NyasaTheme.spacing.xs))
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun BlogDetailScreenPreview() {
    NyasaTheme {
        BlogDetailScreen(
            state = ViewBlogUiState(
                blogPost = BlogPost(
                    pk = 1,
                    title = "Whispers of the Lake: A Journey into" +
                        " the Heart of Mangochi",
                    slug = "whispers-of-the-lake",
                    body = "The sun hung low over the Dedza mountains," +
                        " casting a golden haze across the still waters" +
                        " of Lake Malawi.\n\n" +
                        "\u201CMalawi is often called the Warm Heart of" +
                        " Africa, but here, by the shores of the lake," +
                        " it is the soul that finds its warmth.\u201D" +
                        "\n\nNavigating through the local markets in" +
                        " this lakeside town reveals a vibrant culture.",
                    image = "",
                    date_updated = System.currentTimeMillis(),
                    username = "Mphatso K.",
                    category = "Travel & Culture",
                    tags = "Malawi, TravelJournal, Photography",
                    reading_time = 8,
                    like_count = 24
                ),
                isAuthorOfBlogPost = true,
                comments = listOf(
                    Comment(
                        pk = 1,
                        body = "Beautiful writing!",
                        username = "Reader1",
                        dateCreated = 0L
                    ),
                    Comment(
                        pk = 2,
                        body = "Loved this article.",
                        username = "Reader2",
                        dateCreated = 0L
                    )
                ),
                isLiked = true,
                isBookmarked = false,
                likeCount = 24,
                currentUsername = "Reader1"
            ),
            isLoading = false,
            onAction = {}
        )
    }
}
