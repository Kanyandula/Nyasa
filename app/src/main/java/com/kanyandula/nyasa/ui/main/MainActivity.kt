package com.kanyandula.nyasa.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.auth.AuthActivity
import com.kanyandula.nyasa.ui.components.NyasaBottomBar
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
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedScreen
import com.kanyandula.nyasa.ui.main.blog.composables.BookmarksScreen
import com.kanyandula.nyasa.ui.main.blog.composables.EditBlogAction
import com.kanyandula.nyasa.ui.main.blog.composables.EditBlogScreen
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.AuthorProfileViewModel
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BookmarksViewModel
import com.kanyandula.nyasa.ui.main.create_blog.CreateBlogViewModel
import com.kanyandula.nyasa.ui.main.create_blog.composables.CreateBlogScreen
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.navigation.createImagePickerIntent
import com.kanyandula.nyasa.ui.navigation.handleStandardEvent
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_MUST_SELECT_IMAGE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val AUTH_TOKEN_BUNDLE_KEY = "auth_token"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSession(savedInstanceState)

        setContent {
            NyasaTheme {
                val navController = rememberNavController()
                val token by sessionManager.cachedToken.collectAsStateWithLifecycle()

                LaunchedEffect(token) {
                    if (token == null || token?.account_pk == -1 || token?.token == null) {
                        navAuthActivity()
                    }
                }

                Scaffold(
                    bottomBar = { NyasaBottomBar(navController = navController) }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = Routes.BLOG_GRAPH,
                        modifier = Modifier.padding(padding)
                    ) {
                        navigation(startDestination = Routes.BLOG_FEED, route = Routes.BLOG_GRAPH) {
                            composable(Routes.BLOG_FEED) { entry ->
                                val parentEntry = remember(entry) {
                                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                                }
                                val vm: BlogViewModel = hiltViewModel(parentEntry)
                                val state by vm.viewState.collectAsStateWithLifecycle()

                                BlogFeedScreen(
                                    pagingDataFlow = vm.pagingDataFlow,
                                    state = state,
                                    onAction = { action ->
                                        when (action) {
                                            is BlogFeedAction.BlogClicked ->
                                                navController.navigate(Routes.blogDetail(action.slug))
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
                                            is BlogFeedAction.Refresh -> vm.executeSearch()
                                        }
                                    }
                                )
                            }
                            composable(
                                route = Routes.BLOG_DETAIL,
                                arguments = listOf(navArgument("slug") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                                val parentEntry = remember(backStackEntry) {
                                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                                }
                                val vm: BlogViewModel = hiltViewModel(parentEntry)
                                val accountVm: AccountViewModel = hiltViewModel()
                                val accountState by accountVm.viewState
                                    .collectAsStateWithLifecycle()
                                LaunchedEffect(accountState.accountProperties?.username) {
                                    accountState.accountProperties?.username?.let {
                                        vm.setCurrentUsername(it)
                                    }
                                }
                                BlogDetailRoute(
                                    slug = slug,
                                    viewModel = vm,
                                    onNavigateBack = { navController.popBackStack() },
                                    onEdit = { blogSlug ->
                                        navController.navigate(Routes.blogEdit(blogSlug))
                                    },
                                    onDeleted = { navController.popBackStack() },
                                    onAuthorClick = { username ->
                                        navController.navigate(Routes.authorProfile(username))
                                    }
                                )
                            }
                            composable(
                                route = Routes.BLOG_EDIT,
                                arguments = listOf(navArgument("slug") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                                val parentEntry = remember(backStackEntry) {
                                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                                }
                                val vm: BlogViewModel = hiltViewModel(parentEntry)
                                EditBlogRoute(
                                    slug = slug,
                                    viewModel = vm,
                                    activity = this@MainActivity,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSaved = { navController.popBackStack() }
                                )
                            }
                        }

                        composable(
                            route = Routes.AUTHOR_PROFILE,
                            arguments = listOf(
                                navArgument("username") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments
                                ?.getString("username").orEmpty()
                            AuthorProfileRoute(
                                username = username,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.BOOKMARKS) {
                            BookmarksRoute(
                                onBlogClick = { slug ->
                                    navController.navigate(Routes.blogDetail(slug))
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.CREATE) {
                            val vm: CreateBlogViewModel = hiltViewModel()
                            CreateBlogRoute(
                                viewModel = vm,
                                activity = this@MainActivity
                            )
                        }

                        navigation(startDestination = Routes.ACCOUNT_PROFILE, route = Routes.ACCOUNT_GRAPH) {
                            composable(Routes.ACCOUNT_PROFILE) { entry ->
                                val parentEntry = remember(entry) {
                                    navController.getBackStackEntry(Routes.ACCOUNT_GRAPH)
                                }
                                val vm: AccountViewModel = hiltViewModel(parentEntry)
                                AccountProfileRoute(
                                    viewModel = vm,
                                    onEditProfile = { navController.navigate(Routes.ACCOUNT_EDIT) },
                                    onChangePassword = {
                                        navController.navigate(Routes.ACCOUNT_CHANGE_PASSWORD)
                                    },
                                    onBookmarks = {
                                        navController.navigate(Routes.BOOKMARKS)
                                    }
                                )
                            }
                            composable(Routes.ACCOUNT_EDIT) { entry ->
                                val parentEntry = remember(entry) {
                                    navController.getBackStackEntry(Routes.ACCOUNT_GRAPH)
                                }
                                val vm: AccountViewModel = hiltViewModel(parentEntry)
                                EditAccountRoute(
                                    viewModel = vm,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable(Routes.ACCOUNT_CHANGE_PASSWORD) { entry ->
                                val parentEntry = remember(entry) {
                                    navController.getBackStackEntry(Routes.ACCOUNT_GRAPH)
                                }
                                val vm: AccountViewModel = hiltViewModel(parentEntry)
                                ChangePasswordRoute(
                                    viewModel = vm,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun restoreSession(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        savedInstanceState?.getParcelable<AuthToken>(AUTH_TOKEN_BUNDLE_KEY)?.let {
            sessionManager.login(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY, sessionManager.cachedToken.value)
    }

    private fun navAuthActivity() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}

// region Route Composables

@Composable
private fun BlogDetailRoute(
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

    val context = androidx.compose.ui.platform.LocalContext.current
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
                is BlogDetailAction.EditClicked -> {
                    viewModel.getBlogPost()?.let { blogPost ->
                        viewModel.setUpdatedBlogFields(
                            title = blogPost.title,
                            body = blogPost.body,
                            uri = blogPost.image.toUri(),
                            category = blogPost.category,
                            tags = blogPost.tags
                        )
                        onEdit(slug)
                    }
                }
                is BlogDetailAction.DeleteClicked -> showDeleteDialog = true
                is BlogDetailAction.NavigateBack -> onNavigateBack()
                is BlogDetailAction.LikeClicked -> viewModel.likeBlog(slug)
                is BlogDetailAction.BookmarkClicked -> viewModel.bookmarkBlog(slug)
                is BlogDetailAction.AuthorClicked -> onAuthorClick(action.username)
                is BlogDetailAction.AddComment -> viewModel.addComment(slug, action.body)
                is BlogDetailAction.DeleteComment -> viewModel.deleteComment(action.commentPk)
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
private fun EditBlogRoute(
    slug: String,
    viewModel: BlogViewModel,
    activity: Activity,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.updateBlogState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.setUpdatedBlogFields(title = null, body = null, uri = uri)
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
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
        initialBody = state.updatedBlogBody.orEmpty(),
        imageUri = state.updatedImageUri,
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
                is EditBlogAction.CategorySelected -> viewModel.setUpdatedCategory(action.category)
            }
        }
    )
}

@Composable
private fun CreateBlogRoute(
    viewModel: CreateBlogViewModel,
    activity: Activity
) {
    val blogFields by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = activity as android.content.Context

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModel.setNewBlogFields(title = null, body = null, uri = uri)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            handleStandardEvent(context, event)
        }
    }

    CreateBlogScreen(
        initialTitle = blogFields.blogFields.newBlogTitle.orEmpty(),
        initialBody = blogFields.blogFields.newBlogBody.orEmpty(),
        imageUri = blogFields.blogFields.newImageUri,
        selectedCategory = blogFields.blogFields.category,
        initialTags = blogFields.blogFields.tags.orEmpty(),
        categories = blogFields.categories,
        isLoading = isLoading,
        onPublish = { title, body, tags ->
            viewModel.setTags(tags)
            val imageUri = viewModel.viewState.value.blogFields.newImageUri
            if (imageUri == null) {
                Toast.makeText(context, ERROR_MUST_SELECT_IMAGE, Toast.LENGTH_SHORT).show()
                return@CreateBlogScreen
            }
            viewModel.createNewBlogPost(title, body, imageUri)
        },
        onPickImage = {
            imagePickerLauncher.launch(createImagePickerIntent(activity))
        },
        onCategorySelected = { viewModel.setCategory(it) }
    )
}

@Composable
private fun AccountProfileRoute(
    viewModel: AccountViewModel,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onBookmarks: () -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

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
private fun EditAccountRoute(
    viewModel: AccountViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
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
private fun ChangePasswordRoute(
    viewModel: AccountViewModel,
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
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
private fun AuthorProfileRoute(
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
private fun BookmarksRoute(
    onBlogClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: BookmarksViewModel = hiltViewModel()
    val state by vm.viewState.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

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

// endregion

@Composable
private fun ConfirmDialog(
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
