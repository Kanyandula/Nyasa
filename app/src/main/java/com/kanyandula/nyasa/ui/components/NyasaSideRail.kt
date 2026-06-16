package com.kanyandula.nyasa.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kanyandula.nyasa.ui.navigation.MainNavItem
import com.kanyandula.nyasa.ui.navigation.mainNavItemForDestination
import com.kanyandula.nyasa.ui.navigation.navigateToMainNavItem
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun NyasaSideRail(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentItem = mainNavItemForDestination(navBackStackEntry?.destination)

    NyasaSideRailContent(
        currentItem = currentItem,
        onItemSelected = navController::navigateToMainNavItem,
        modifier = modifier
    )
}

@Composable
fun NyasaSideRailContent(
    currentItem: MainNavItem,
    onItemSelected: (MainNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        val itemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = Color.Transparent
        )
        MainNavItem.entries.forEach { item ->
            val selected = item == currentItem
            NavigationRailItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = { MainNavItemIcon(item = item, selected = selected) },
                label = { MainNavItemLabel(item = item) },
                colors = itemColors
            )
        }
    }
}

@Preview
@Composable
private fun NyasaSideRailPreview() {
    NyasaTheme {
        NyasaSideRailContent(
            currentItem = MainNavItem.Home,
            onItemSelected = {}
        )
    }
}
