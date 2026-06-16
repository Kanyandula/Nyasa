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
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.Features
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
 * every route in [Routes.BlogGraph] to avoid drifting copies.
 */
@Composable
private fun BindCurrentUsernameToBlogVm(vm: BlogViewModel) {
    val accountVm: AccountViewModel = hiltViewModel()
    val accountState by accountVm.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(accountState.accountProperties?.username) {
        accountState.accountProperties?.username?.let { vm.setCurrentUsername(it) }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ExpandedFeedPane(
    mode: FeedMode,
    vm: BlogViewModel,
    state: BlogListUiState,
    feedAction: (BlogFeedAction) -> Unit,
    onBlogClicked: (String) -> Unit,
    featuredHero: BlogPost?,
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
            featuredHero = featuredHero,
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
    val featuredHero by vm.featuredHero.collectAsStateWithLifecycle()
    BlogListDetailScaffold(
        onNavigateToDetailFullScreen = { slug -> navController.navigate(Routes.BlogDetail(slug)) },
        visibleSlugs = visibleSlugs,
        mode = mode,
        listPane = { onBlogClicked ->
            BlogFeedScreen(
                pagingDataFlow = vm.pagingDataFlow,
                state = state,
                mode = mode,
                featuredHero = featuredHero,
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
                onEdit = { blogSlug -> navController.navigate(Routes.BlogEdit(blogSlug)) },
                onDeleted = onClose,
                onAuthorClick = { username -> navController.navigate(Routes.AuthorProfile(username)) }
            )
        },
        expandedListPane = if (Features.ADAPTIVE_LAYOUT_ENABLED) {
            { onBlogClicked ->
                ExpandedFeedPane(
                    mode = mode,
                    vm = vm,
                    state = state,
                    feedAction = feedAction,
                    onBlogClicked = onBlogClicked,
                    featuredHero = featuredHero,
                    onVisibleSlugsChanged = onVisibleSlugsChanged
                )
            }
        } else {
            null
        }
    )
}

/**
 * Registers a feed-style route (Home or Search) wrapped in [BlogListDetailScaffold]. On
 * Small windows the wrapper passes clicks through to a full-screen [Routes.BlogDetail]; on
 * Medium it owns selection inside the scaffold's pane navigator.
 */
private inline fun <reified T : Any> NavGraphBuilder.blogFeedRoute(
    mode: FeedMode,
    navController: NavController
) {
    composable<T> { entry ->
        val parentEntry = remember(entry) {
            navController.getBackStackEntry<Routes.BlogGraph>()
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
    navigation<Routes.MainGraph>(startDestination = Routes.BlogGraph) {
        navigation<Routes.BlogGraph>(startDestination = Routes.BlogFeed) {
            blogFeedRoute<Routes.BlogFeed>(FeedMode.Home, navController)
            blogFeedRoute<Routes.BlogSearch>(FeedMode.Search, navController)
            composable<Routes.BlogDetail> { backStackEntry ->
                val slug = backStackEntry.toRoute<Routes.BlogDetail>().slug
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Routes.BlogGraph>()
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                BindCurrentUsernameToBlogVm(vm)
                BlogDetailRoute(
                    slug = slug,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onEdit = { blogSlug ->
                        navController.navigate(Routes.BlogEdit(blogSlug))
                    },
                    onDeleted = { navController.popBackStack() },
                    onAuthorClick = { username ->
                        navController.navigate(Routes.AuthorProfile(username))
                    }
                )
            }
            composable<Routes.BlogEdit> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry<Routes.BlogGraph>()
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val slug = backStackEntry.toRoute<Routes.BlogEdit>().slug
                EditBlogRoute(
                    slug = slug,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }

        composable<Routes.AuthorProfile> { backStackEntry ->
            val username = backStackEntry.toRoute<Routes.AuthorProfile>().username
            AuthorProfileRoute(
                username = username,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Routes.Bookmarks> {
            BookmarksRoute(
                onBlogClick = { slug ->
                    navController.navigate(Routes.BlogDetail(slug))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Routes.Create> {
            val vm: CreateBlogViewModel = hiltViewModel()
            CreateBlogRoute(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        navigation<Routes.AccountGraph>(startDestination = Routes.AccountProfile) {
            composable<Routes.AccountProfile> { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry<Routes.AccountGraph>()
                }
                val vm: AccountViewModel = hiltViewModel(parentEntry)
                AccountProfileRoute(
                    viewModel = vm,
                    currentTheme = currentTheme,
                    onThemeChanged = onThemeChanged,
                    onEditProfile = { navController.navigate(Routes.AccountEdit) },
                    onChangePassword = {
                        navController.navigate(Routes.AccountChangePassword)
                    },
                    onBookmarks = {
                        navController.navigate(Routes.Bookmarks)
                    }
                )
            }
            composable<Routes.AccountEdit> { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry<Routes.AccountGraph>()
                }
                val vm: AccountViewModel = hiltViewModel(parentEntry)
                EditAccountRoute(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Routes.AccountChangePassword> { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry<Routes.AccountGraph>()
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
