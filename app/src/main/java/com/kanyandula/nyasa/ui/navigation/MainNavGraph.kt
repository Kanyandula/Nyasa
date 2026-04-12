package com.kanyandula.nyasa.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.kanyandula.nyasa.ui.main.blog.composables.BlogFeedScreen
import com.kanyandula.nyasa.ui.main.blog.composables.FeedMode
import com.kanyandula.nyasa.ui.main.blog.viewmodel.BlogViewModel
import com.kanyandula.nyasa.ui.main.create_blog.CreateBlogViewModel
import com.kanyandula.nyasa.ui.main.handleBlogFeedAction

@Suppress("LongMethod")
fun NavGraphBuilder.mainGraph(navController: NavController) {
    navigation(startDestination = Routes.BLOG_GRAPH, route = Routes.MAIN_GRAPH) {
        navigation(startDestination = Routes.BLOG_FEED, route = Routes.BLOG_GRAPH) {
            composable(Routes.BLOG_FEED) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val state by vm.viewState.collectAsStateWithLifecycle()

                val feedAction = remember(vm, navController) {
                    handleBlogFeedAction(vm, navController)
                }
                BlogFeedScreen(
                    pagingDataFlow = vm.pagingDataFlow,
                    state = state,
                    mode = FeedMode.Home,
                    onAction = feedAction
                )
            }
            composable(Routes.BLOG_SEARCH) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Routes.BLOG_GRAPH)
                }
                val vm: BlogViewModel = hiltViewModel(parentEntry)
                val state by vm.viewState.collectAsStateWithLifecycle()

                val feedAction = remember(vm, navController) {
                    handleBlogFeedAction(vm, navController)
                }
                BlogFeedScreen(
                    pagingDataFlow = vm.pagingDataFlow,
                    state = state,
                    mode = FeedMode.Search,
                    onAction = feedAction
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
                val accountState by accountVm.viewState.collectAsStateWithLifecycle()
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
