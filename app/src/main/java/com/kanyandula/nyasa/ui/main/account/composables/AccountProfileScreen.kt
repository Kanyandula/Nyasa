package com.kanyandula.nyasa.ui.main.account.composables

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.ProfileAvatar
import com.kanyandula.nyasa.ui.main.account.state.AccountViewState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.ThemePreference
import com.kanyandula.nyasa.util.analytics.TrackScreen

@Composable
fun AccountProfileScreen(
    state: AccountViewState,
    isLoading: Boolean,
    currentTheme: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    onAction: (AccountProfileAction) -> Unit
) {
    TrackScreen("AccountProfile")
    val account = state.accountProperties
    val email = account?.email.orEmpty()
    val username = account?.username.orEmpty()
    val profileImage = account?.profile_image
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NyasaTheme.spacing.l, vertical = NyasaTheme.spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(NyasaTheme.spacing.m))

            ProfileAvatar(imageUrl = profileImage, size = 96.dp)

            Spacer(Modifier.height(NyasaTheme.spacing.m))

            Text(
                text = username.ifEmpty { "Creator Name" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(NyasaTheme.spacing.xs))
            Text(
                text = "@${username.ifEmpty { "username" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            // Account info card
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(NyasaTheme.spacing.m)) {
                    AccountInfoRow(
                        label = "EMAIL ADDRESS",
                        value = email.ifEmpty { "email@example.com" },
                        icon = Icons.Filled.Email
                    )
                    Spacer(Modifier.height(NyasaTheme.spacing.m))
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                    Spacer(Modifier.height(NyasaTheme.spacing.m))
                    AccountInfoRow(
                        label = "ACCOUNT TYPE",
                        value = "Premium Storyteller",
                        icon = Icons.Filled.Stars
                    )
                }
            }

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            // Account Settings section
            Text(
                text = "ACCOUNT SETTINGS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = NyasaTheme.spacing.s)
            )

            AppearanceSelector(
                currentTheme = currentTheme,
                onThemeChanged = onThemeChanged
            )
            Spacer(modifier = Modifier.height(NyasaTheme.spacing.m))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.medium
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Filled.EditNote,
                        label = "Edit Profile",
                        onClick = { onAction(AccountProfileAction.EditProfile) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                    AccountMenuItem(
                        icon = Icons.Filled.LockReset,
                        label = "Change Password",
                        onClick = { onAction(AccountProfileAction.ChangePassword) }
                    )
                }
            }

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            TextButton(onClick = { onAction(AccountProfileAction.Logout) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(NyasaTheme.spacing.s))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(NyasaTheme.spacing.l))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    12.dp,
                    Alignment.CenterHorizontally
                )
            ) {
                StatCard(
                    value = "24",
                    label = "PUBLISHED\nSTORIES",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "1.2k",
                    label = "TOTAL\nREADS",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(NyasaTheme.spacing.l))
        }

        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun AppearanceSelector(
    currentTheme: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = NyasaTheme.spacing.m)) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(NyasaTheme.spacing.s))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemePreference.entries.forEachIndexed { index, preference ->
                SegmentedButton(
                    selected = currentTheme == preference,
                    onClick = { onThemeChanged(preference) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemePreference.entries.size
                    )
                ) {
                    Text(
                        text = when (preference) {
                            ThemePreference.SYSTEM -> "System"
                            ThemePreference.LIGHT -> "Light"
                            ThemePreference.DARK -> "Dark"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(NyasaTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(NyasaTheme.spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
                .padding(horizontal = NyasaTheme.spacing.m, vertical = NyasaTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(NyasaTheme.spacing.m))
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
            state = AccountViewState(
                accountProperties = AccountProperties(
                    pk = 1,
                    email = "creator@nyasablog.mw",
                    username = "nyasa_creator",
                    bio = "Storyteller from the warm heart of Africa",
                    location = "Lilongwe, Malawi"
                )
            ),
            isLoading = false,
            currentTheme = ThemePreference.SYSTEM,
            onThemeChanged = {},
            onAction = {}
        )
    }
}
