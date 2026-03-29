package com.kanyandula.nyasa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.SunsetOrange

enum class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("Home", Routes.BLOG_GRAPH, Icons.Filled.Home, Icons.Outlined.Home),
    Create("Create", Routes.CREATE, Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    Account("Account", Routes.ACCOUNT_GRAPH, Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun NyasaBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentItem = when {
        currentRoute?.startsWith("blog") == true -> BottomNavItem.Home
        currentRoute?.startsWith("create") == true -> BottomNavItem.Create
        currentRoute?.startsWith("account") == true -> BottomNavItem.Account
        else -> BottomNavItem.Home
    }

    NyasaBottomBarContent(
        currentItem = currentItem,
        onItemSelected = { item ->
            navController.navigate(item.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier
    )
}

@Composable
fun NyasaBottomBarContent(
    currentItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 0.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = Color.Transparent
        )
        BottomNavItem.entries.forEach { item ->
            val selected = item == currentItem
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .offset(y = 14.dp)
                                    .size(6.dp)
                                    .background(SunsetOrange, CircleShape)
                            )
                        }
                    }
                },
                label = { Text(text = item.label, style = MaterialTheme.typography.labelSmall) },
                colors = itemColors
            )
        }
    }
}

@Preview
@Composable
private fun NyasaBottomBarPreview() {
    NyasaTheme {
        NyasaBottomBarContent(
            currentItem = BottomNavItem.Home,
            onItemSelected = {}
        )
    }
}
