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
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.auth.AuthActivity
import com.kanyandula.nyasa.ui.components.NyasaBottomBar
import com.kanyandula.nyasa.ui.main.account.AccountViewModel
import com.kanyandula.nyasa.ui.main.account.composables.AccountProfileScreen
import com.kanyandula.nyasa.ui.main.account.composables.ChangePasswordScreen
import com.kanyandula.nyasa.ui.main.account.composables.EditAccountScreen
import com.kanyandula.nyasa.ui.main.account.state.AccountUiEvent
import com.kanyandula.nyasa.ui.main.blog.composables.BlogDetailScreen
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedScreen
import com.kanyandula.nyasa.ui.main.blog.composables.EditBlogScreen
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
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
                                    searchQuery = state.searchQuery,
                                    currentFilter = state.filter,
                                    currentOrder = state.order,
                                    onBlogClick = { slug ->
                                        navController.navigate(Routes.blogDetail(slug))
                                    },
                                    onSearch = { query ->
                                        vm.setQuery(query)
                                        vm.executeSearch()
                                    },
                                    onFilterApply = { filter, order ->
                                        vm.setBlogFilter(filter)
                                        vm.setBlogOrder(order)
                                        vm.saveFilterOptions(filter, order)
                                        vm.executeSearch()
                                    },
                                    onRefresh = { vm.executeSearch() }
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
                                BlogDetailRoute(
                                    slug = slug,
                                    viewModel = vm,
                                    onNavigateBack = { navController.popBackStack() },
                                    onEdit = { blogSlug ->
                                        navController.navigate(Routes.blogEdit(blogSlug))
                                    },
                                    onDeleted = { navController.popBackStack() }
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
    onDeleted: () -> Unit
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
        blogPost = state.blogPost,
        isAuthor = state.isAuthorOfBlogPost,
        isLoading = isLoading,
        onEditClick = {
            val blogPost = viewModel.getBlogPost() ?: return@BlogDetailScreen
            viewModel.setUpdatedBlogFields(blogPost.title, blogPost.body, blogPost.image.toUri())
            onEdit(slug)
        },
        onDeleteClick = { showDeleteDialog = true },
        onNavigateBack = onNavigateBack
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
        isLoading = isLoading,
        onSave = { title, body ->
            viewModel.updateBlogPost(slug, title, body, viewModel.getUpdatedBlogUri())
        },
        onPickImage = {
            imagePickerLauncher.launch(createImagePickerIntent(activity))
        },
        onNavigateBack = onNavigateBack
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
        isLoading = isLoading,
        onPublish = { title, body ->
            val imageUri = viewModel.viewState.value.blogFields.newImageUri
            if (imageUri == null) {
                Toast.makeText(context, ERROR_MUST_SELECT_IMAGE, Toast.LENGTH_SHORT).show()
                return@CreateBlogScreen
            }
            viewModel.createNewBlogPost(title, body, imageUri)
        },
        onPickImage = {
            imagePickerLauncher.launch(createImagePickerIntent(activity))
        }
    )
}

@Composable
private fun AccountProfileRoute(
    viewModel: AccountViewModel,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit
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
        email = state.accountProperties?.email.orEmpty(),
        username = state.accountProperties?.username.orEmpty(),
        isLoading = isLoading,
        onEditProfile = onEditProfile,
        onChangePassword = onChangePassword,
        onLogout = { viewModel.logout() }
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

    EditAccountScreen(
        initialEmail = state.accountProperties?.email.orEmpty(),
        initialUsername = state.accountProperties?.username.orEmpty(),
        isLoading = isLoading,
        onSave = { email, username ->
            viewModel.saveAccountProperties(email, username)
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
