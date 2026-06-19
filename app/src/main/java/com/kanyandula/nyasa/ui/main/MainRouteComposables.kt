package com.kanyandula.nyasa.ui.main

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.ui.components.RequestNotificationPermissionEffect
import com.kanyandula.nyasa.ui.main.account.AccountViewModel
import com.kanyandula.nyasa.ui.main.account.composables.AccountProfileAction
import com.kanyandula.nyasa.ui.main.account.composables.AccountProfileScreen
import com.kanyandula.nyasa.ui.main.account.composables.ChangePasswordScreen
import com.kanyandula.nyasa.ui.main.account.composables.EditAccountScreen
import com.kanyandula.nyasa.ui.main.account.state.AccountUiEvent
import com.kanyandula.nyasa.ui.main.blog.composables.AuthorProfileScreen
import com.kanyandula.nyasa.ui.main.blog.composables.BlogDetailAction
import com.kanyandula.nyasa.ui.main.blog.composables.BlogDetailScreen
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedAction
import com.kanyandula.nyasa.ui.main.blog.composables.BookmarksScreen
import com.kanyandula.nyasa.ui.main.blog.composables.EditBlogAction
import com.kanyandula.nyasa.ui.main.blog.composables.EditBlogScreen
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.AuthorProfileViewModel
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BookmarksViewModel
import com.kanyandula.nyasa.ui.main.blog.viewmodel.EditBlogViewModel
import com.kanyandula.nyasa.ui.main.create_blog.CreateBlogViewModel
import com.kanyandula.nyasa.ui.main.create_blog.composables.CreateBlogScreen
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogNavigationEvent
import com.kanyandula.nyasa.ui.navigation.MainNavItem
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.navigation.createImagePickerIntent
import com.kanyandula.nyasa.ui.navigation.handleStandardEvent
import com.kanyandula.nyasa.ui.navigation.isResumed
import com.kanyandula.nyasa.ui.navigation.navigateToMainNavItem
import com.kanyandula.nyasa.ui.theme.ThemePreference

internal fun handleBlogFeedAction(
    vm: BlogViewModel,
    navController: NavController,
    entry: NavBackStackEntry
): (BlogFeedAction) -> Unit = { action ->
    when (action) {
        is BlogFeedAction.BlogClicked ->
            navController.navigate(Routes.BlogDetail(action.slug))
        is BlogFeedAction.Search -> {
            vm.setQuery(action.query)
            vm.executeSearch()
        }
        is BlogFeedAction.FilterApply -> {
            vm.setBlogFilter(action.filter)
            vm.setBlogOrder(action.order)
            vm.saveFilterOptions(action.filter, action.order)
            vm.executeSearch()
        }
        is BlogFeedAction.CategorySelected ->
            vm.setSelectedCategory(action.category)
        is BlogFeedAction.BookmarkClicked ->
            vm.bookmarkBlog(action.slug)
        is BlogFeedAction.CreateClicked ->
            navController.navigate(Routes.Create)
        is BlogFeedAction.BackClicked ->
            // Today only SearchTopBar dispatches this. Route through the bottom-bar helper so
            // we always land on Home in a single transition — `popBackStack()` could expose an
            // intermediate NavGraph entry (no Composable) and render blank between taps. The
            // RESUMED gate drops a second back tap that lands while the first is still settling.
            if (entry.isResumed()) navController.navigateToMainNavItem(MainNavItem.Home)
        is BlogFeedAction.Refresh -> vm.executeSearch()
    }
}

@Composable
internal fun BlogDetailRoute(
    slug: String,
    viewModel: BlogViewModel,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    onAuthorClick: (String) -> Unit
) {
    val state by viewModel.viewBlogState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(slug) {
        viewModel.loadBlogBySlug(slug)
        viewModel.checkIsAuthorOfBlogPost(slug)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BlogNavigationEvent.BlogDeleted -> onDeleted()
                else -> handleStandardEvent(context, event)
            }
        }
    }

    BlogDetailScreen(
        state = state,
        isLoading = isLoading,
        onAction = { action ->
            when (action) {
                // EditBlogViewModel reloads the post by slug, so no field seeding here.
                is BlogDetailAction.EditClicked -> onEdit(slug)
                is BlogDetailAction.DeleteClicked -> showDeleteDialog = true
                is BlogDetailAction.NavigateBack -> onNavigateBack()
                is BlogDetailAction.LikeClicked -> viewModel.likeBlog(slug)
                is BlogDetailAction.BookmarkClicked -> viewModel.bookmarkBlog(slug)
                is BlogDetailAction.AuthorClicked -> onAuthorClick(action.username)
                is BlogDetailAction.AddComment -> viewModel.addComment(slug, action.body)
                is BlogDetailAction.DeleteComment -> viewModel.deleteComment(action.commentPk, action.slug)
            }
        }
    )

    if (showDeleteDialog) {
        ConfirmDialog(
            message = stringResource(R.string.are_you_sure_delete),
            onConfirm = {
                viewModel.deleteBlogPost()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
internal fun EditBlogRoute(
    slug: String,
    viewModel: EditBlogViewModel,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    RequestNotificationPermissionEffect()

    val activity = LocalContext.current as Activity
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(slug) { viewModel.loadBlogForEdit(slug) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.setUpdatedImageUri(uri)
            }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BlogNavigationEvent.BlogUpdateSuccess -> onSaved()
                else -> handleStandardEvent(context, event)
            }
        }
    }

    EditBlogScreen(
        initialTitle = state.updatedBlogTitle.orEmpty(),
        bodyState = viewModel.editBodyState,
        imageModel = state.updatedImageUri ?: state.originalImageUrl,
        selectedCategory = state.updatedCategory,
        initialTags = state.updatedTags.orEmpty(),
        categories = state.categories,
        isLoading = isLoading,
        onAction = { action ->
            when (action) {
                is EditBlogAction.Save -> {
                    viewModel.setUpdatedTags(action.tags)
                    viewModel.updateBlogPost(
                        slug,
                        action.title,
                        action.body,
                        viewModel.getUpdatedBlogUri()
                    )
                }
                is EditBlogAction.PickImage ->
                    imagePickerLauncher.launch(createImagePickerIntent(activity))
                is EditBlogAction.NavigateBack -> onNavigateBack()
                is EditBlogAction.CategorySelected ->
                    viewModel.setUpdatedCategory(action.category)
            }
        }
    )
}

@Composable
internal fun CreateBlogRoute(
    viewModel: CreateBlogViewModel,
    onNavigateBack: () -> Unit
) {
    RequestNotificationPermissionEffect()

    val activity = LocalContext.current as Activity
    val blogFields by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.setNewBlogFields(uri = uri)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateBlogNavigationEvent.BlogCreated -> onNavigateBack()
                else -> handleStandardEvent(context, event)
            }
        }
    }

    CreateBlogScreen(
        initialTitle = blogFields.blogFields.newBlogTitle.orEmpty(),
        bodyState = viewModel.createBodyState,
        imageModel = blogFields.blogFields.newImageUri,
        selectedCategory = blogFields.blogFields.category,
        initialTags = blogFields.blogFields.tags.orEmpty(),
        categories = blogFields.categories,
        isLoading = isLoading,
        onPublish = { title, body, tags ->
            viewModel.setTags(tags)
            val imageUri = viewModel.viewState.value.blogFields.newImageUri
            if (imageUri == null) {
                Toast.makeText(context, "You must select an image.", Toast.LENGTH_SHORT).show()
                return@CreateBlogScreen
            }
            viewModel.createNewBlogPost(title, body, imageUri)
        },
        onPickImage = {
            imagePickerLauncher.launch(createImagePickerIntent(activity))
        },
        onCategorySelected = { viewModel.setCategory(it) },
        onNavigateBack = onNavigateBack,
        onSaveDraft = onNavigateBack
    )
}

@Composable
internal fun AccountProfileRoute(
    viewModel: AccountViewModel,
    currentTheme: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onBookmarks: () -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getAccountProperties()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            handleStandardEvent(context, event)
        }
    }

    AccountProfileScreen(
        state = state,
        isLoading = isLoading,
        currentTheme = currentTheme,
        onThemeChanged = onThemeChanged,
        onAction = { action ->
            when (action) {
                is AccountProfileAction.EditProfile -> onEditProfile()
                is AccountProfileAction.ChangePassword -> onChangePassword()
                is AccountProfileAction.Bookmarks -> onBookmarks()
                is AccountProfileAction.Logout -> viewModel.logout()
            }
        }
    )
}

@Composable
internal fun EditAccountRoute(
    viewModel: AccountViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            handleStandardEvent(context, event)
        }
    }

    val account = state.accountProperties
    EditAccountScreen(
        initialEmail = account?.email.orEmpty(),
        initialUsername = account?.username.orEmpty(),
        initialBio = account?.bio.orEmpty(),
        initialLocation = account?.location.orEmpty(),
        initialWebsite = account?.website.orEmpty(),
        initialTwitter = account?.twitter.orEmpty(),
        initialFacebook = account?.facebook.orEmpty(),
        initialInstagram = account?.instagram.orEmpty(),
        initialLinkedin = account?.linkedin.orEmpty(),
        isLoading = isLoading,
        onSave = { formData ->
            viewModel.saveAccountProperties(formData.email, formData.username)
            viewModel.updateProfile(
                ProfileUpdateRequest(
                    bio = formData.bio.ifBlank { null },
                    location = formData.location.ifBlank { null },
                    website = formData.website.ifBlank { null },
                    twitter = formData.twitter.ifBlank { null },
                    facebook = formData.facebook.ifBlank { null },
                    instagram = formData.instagram.ifBlank { null },
                    linkedin = formData.linkedin.ifBlank { null }
                )
            )
        },
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun ChangePasswordRoute(
    viewModel: AccountViewModel,
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountUiEvent.PasswordChanged -> onNavigateBack()
                else -> handleStandardEvent(context, event)
            }
        }
    }

    ChangePasswordScreen(
        isLoading = isLoading,
        onUpdatePassword = { current, new, confirmNew ->
            viewModel.changePassword(current, new, confirmNew)
        },
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun AuthorProfileRoute(
    username: String,
    onNavigateBack: () -> Unit
) {
    val vm: AuthorProfileViewModel = hiltViewModel()
    val state by vm.viewState.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(username) {
        vm.loadProfile(username)
    }

    AuthorProfileScreen(
        profile = state.profile,
        isLoading = isLoading,
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun BookmarksRoute(
    onBlogClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: BookmarksViewModel = hiltViewModel()
    val state by vm.viewState.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.events.collect { event -> handleStandardEvent(context, event) }
    }

    BookmarksScreen(
        bookmarks = state.bookmarks,
        isLoading = isLoading,
        onBlogClick = onBlogClick,
        onRemoveBookmark = { slug -> vm.removeBookmark(slug) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
internal fun ConfirmDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.are_you_sure),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.text_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel))
            }
        }
    )
}
