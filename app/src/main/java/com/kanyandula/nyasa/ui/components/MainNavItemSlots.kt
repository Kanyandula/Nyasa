package com.kanyandula.nyasa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.navigation.MainNavItem
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
internal fun MainNavItemIcon(item: MainNavItem, selected: Boolean) {
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
                    .background(NyasaTheme.colors.sunsetGradientEnd, CircleShape)
            )
        }
    }
}

@Composable
internal fun MainNavItemLabel(item: MainNavItem) {
    Text(text = item.label, style = MaterialTheme.typography.labelSmall)
}
