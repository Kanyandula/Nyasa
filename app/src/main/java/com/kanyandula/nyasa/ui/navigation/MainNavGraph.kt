package com.kanyandula.nyasa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.kanyandula.nyasa.ui.main.AccountProfileRoute
import com.kanyandula.nyasa.ui.main.AuthorProfileRoute
import com.kanyandula.nyasa.ui.main.BlogDetailRoute
import com.kanyandula.nyasa.ui.main.BookmarksRoute
import com.kanyandula.nyasa.ui.main.ChangePasswordRoute
import com.kanyandula.nyasa.ui.main.CreateBlogRoute
import com.kanyandula.nyasa.ui.main.EditAccountRoute
import com.kanyandula.nyasa.ui.main.EditBlogRoute
import com.kanyandula.nyasa.ui.main.account.AccountViewModel
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedAction
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedScreen
import com.kanyandula.nyasa.ui.main.blog.composables.BlogListDetailScaffold
import com.kanyandula.nyasa.ui.main.blog.composables.FeedMode
import com.kanyandula.nyasa.ui.main.blog.composables.HomeFeedExpanded
import com.kanyandula.nyasa.ui.main.blog.composables.SearchFeedExpanded
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import com.kanyandula.nyasa.ui.main.create_blog.CreateBlogViewModel
import com.kanyandula.nyasa.ui.main.handleBlogFeedAction
import com.kanyandula.nyasa.ui.theme.ThemePreference

private val StringSetSaver: Saver<Set<String>, Any> = listSaver(
    save = { it.toList() },
    restore = { (it as List<*>).filterIsInstance<String>().toSet() }
)

/**
 * Binds the signed-in account's username into the supplied [BlogViewModel] so the
 * downstream BlogDetail / Feed flows can determine bookmark-as-author state. Shared by
 * every route in `BLOG_GRAPH` to avoid drifting copies.
 */
@Composable
private fun BindCurrentUsernameToBlogVm(vm: BlogViewModel) {
    val accountVm: AccountViewModel = hiltViewModel()
    val accountState by accountVm.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(accountState.accountProperties?.username) {
        accountState.accountProperties?.username?.let { vm.setCurrentUsername(it) }
    }
}

@Composable
private fun ExpandedFeedPane(
    mode: FeedMode,
    vm: BlogViewModel,
    state: BlogListUiState,
    feedAction: (BlogFeedAction) -> Unit,
    onBlogClicked: (String) -> Unit,
    onVisibleSlugsChanged: (Set<String>) -> Unit
) {
    when (mode) {
        FeedMode.Home -> HomeFeedExpanded(
            pagingDataFlow = vm.pagingDataFlow,
            state = state,
            onAction = { action ->
                when (action) {
                    is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                    else -> feedAction(action)
                }
            },
            onBlogClicked = onBlogClicked,
            onVisibleSlugsChanged = onVisibleSlugsChanged
        )
        FeedMode.Search -> SearchFeedExpanded(
            pagingDataFlow = vm.pagingDataFlow,
            state = state,
            onAction = { action ->
                when (action) {
                    is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                    else -> feedAction(action)
                }
            },
            onBlogClicked = onBlogClicked,
            onVisibleSlugsChanged = onVisibleSlugsChanged
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun BlogFeedScaffold(
    mode: FeedMode,
    vm: BlogViewModel,
    state: BlogListUiState,
    feedAction: (BlogFeedAction) -> Unit,
    navController: NavController,
    visibleSlugs: Set<String>,
    onVisibleSlugsChanged: (Set<String>) -> Unit
) {
    BlogListDetailScaffold(
        onNavigateToDetailFullScreen = { slug -> navController.navigate(Routes.blogDetail(slug)) },
        visibleSlugs = visibleSlugs,
        mode = mode,
        listPane = { onBlogClicked ->
            BlogFeedScreen(
                pagingDataFlow = vm.pagingDataFlow,
                state = state,
                mode = mode,
                onAction = { action ->
                    when (action) {
                        is BlogFeedAction.BlogClicked -> onBlogClicked(action.slug)
                        else -> feedAction(action)
                    }
                },
                onVisibleSlugsChanged = onVisibleSlugsChanged
            )
        },
        detailPane = { slug, onClose ->
            BlogDetailRoute(
                slug = slug,
                viewModel = vm,
                onNavigateBack = onClose,
                onEdit = { blogSlug -> navController.navigate(Routes.blogEdit(blogSlug)) },
                onDeleted = onClose,
                onAuthorClick = { username -> navController.navigate(Routes.authorProfile(username)) }
            )
        },
        expandedListPane = { onBlogClicked ->
            ExpandedFeedPane(
                mode = mode,
                vm = vm,
                state = state,
                feedAction = feedAction,
                onBlogClicked = onBlogClicked,
                onVisibleSlugsChanged = onVisibleSlugsChanged
            )
        }
    )
}

/**
 * Registers a feed-style route (Home or Search) wrapped in [BlogListDetailScaffold]. On
 * Small windows the wrapper passes clicks through to a full-screen `BLOG_DETAIL`; on
 * Medium it owns selection inside the scaffold's pane navigator.
 */
private fun NavGraphBuilder.blogFeedRoute(
    route: String,
    mode: FeedMode,
    navController: NavController
) {
    composable(route) { entry ->
        val parentEntry = remember(entry) {
            navController.getBackStackEntry(Routes.BLOG_GRAPH)
        }
        val vm: BlogViewModel = hiltViewModel(parentEntry)
        val state by vm.viewState.collectAsStateWithLifecycle()
        BindCurrentUsernameToBlogVm(vm)

        val feedAction = remember(vm, navController) { handleBlogFeedAction(vm, navController) }

        var visibleSlugs by rememberSaveable(stateSaver = StringSetSaver) {
            mutableStateOf(emptySet<String>())
        }

        BlogFeedScaffold(
            mode = mode,
            vm = vm,
            state = state,
            feedAction = feedAction,
            navController = navController,
            visibleSlugs = visibleSlugs,
            onVisibleSlugsChanged = { visibleSlugs = it }
        )
    }
}

@Suppress("LongMethod")
fun NavGraphBuilder.mainGraph(
    navController: NavController,
    currentTheme: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit
) {
    navigation(startDestination = Routes.BLOG_GRAPH, route = Routes.MAIN_GRAPH) {
        navigation(startDestination = Routes.BLOG_FEED, route = Routes.BLOG_GRAPH) {
            blogFeedRoute(Routes.BLOG_FEED, FeedMode.Home, navController)
            blogFeedRoute(Routes.BLOG_SEARCH, FeedMode.Search, navController)
            composable(
                route = Routes.BLOG_DETAIL,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                BindCurrentUsernameToBlogVm(vm)
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
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                EditBlogRoute(
                    slug = slug,
                    viewModel = vm,
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
            val username = backStackEntry.arguments?.getString("username").orEmpty()
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
                onNavigateBack = { navController.popBackStack() }
            )
        }

        navigation(
            startDestination = Routes.ACCOUNT_PROFILE,
            route = Routes.ACCOUNT_GRAPH
        ) {
            composable(Routes.ACCOUNT_PROFILE) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.ACCOUNT_GRAPH)
                }
                val vm: AccountViewModel = hiltViewModel(parentEntry)
                AccountProfileRoute(
                    viewModel = vm,
                    currentTheme = currentTheme,
                    onThemeChanged = onThemeChanged,
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
