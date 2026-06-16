package com.kanyandula.nyasa.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kanyandula.nyasa.ui.navigation.MainNavItem
import com.kanyandula.nyasa.ui.navigation.mainNavItemForDestination
import com.kanyandula.nyasa.ui.navigation.navigateToMainNavItem
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun NyasaBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentItem = mainNavItemForDestination(navBackStackEntry?.destination)

    NyasaBottomBarContent(
        currentItem = currentItem,
        onItemSelected = navController::navigateToMainNavItem,
        modifier = modifier
    )
}

@Composable
fun NyasaBottomBarContent(
    currentItem: MainNavItem,
    onItemSelected: (MainNavItem) -> Unit,
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
        MainNavItem.entries.forEach { item ->
            val selected = item == currentItem
            NavigationBarItem(
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
private fun NyasaBottomBarPreview() {
    NyasaTheme {
        NyasaBottomBarContent(
            currentItem = MainNavItem.Home,
            onItemSelected = {}
        )
    }
}
