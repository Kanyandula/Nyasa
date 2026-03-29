package com.kanyandula.nyasa.ui.main.account.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.components.ButtonStyle
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun AccountProfileScreen(
    email: String,
    username: String,
    isLoading: Boolean,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = username.ifEmpty { "Username" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = email.ifEmpty { "email@example.com" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.medium
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Filled.EditNote,
                        label = "Edit Profile",
                        onClick = onEditProfile
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerLow)
                    AccountMenuItem(
                        icon = Icons.Filled.LockReset,
                        label = "Change Password",
                        onClick = onChangePassword
                    )
                }
            }
            Spacer(Modifier.height(32.dp))

            NyasaButton(
                text = "Logout",
                onClick = onLogout,
                style = ButtonStyle.Destructive,
                trailingIcon = Icons.AutoMirrored.Filled.ExitToApp
            )
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun AccountMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AccountProfileScreenPreview() {
    NyasaTheme {
        AccountProfileScreen(
            email = "creator@nyasablog.mw",
            username = "nyasa_creator",
            isLoading = false,
            onEditProfile = {},
            onChangePassword = {},
            onLogout = {}
        )
    }
}
