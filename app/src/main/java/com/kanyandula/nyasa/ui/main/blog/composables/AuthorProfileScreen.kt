package com.kanyandula.nyasa.ui.main.blog.composables

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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.components.ProfileAvatar
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorProfileScreen(
    profile: UserProfile?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = profile?.username ?: "Author Profile",
                    onNavigationClick = onNavigateBack
                )
            }
        ) { padding ->
            if (profile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = NyasaTheme.spacing.l, vertical = NyasaTheme.spacing.l),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileAvatar(imageUrl = profile.profileImage, size = 96.dp)
                    Spacer(Modifier.height(NyasaTheme.spacing.m))

                    Text(
                        text = profile.username,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!profile.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(NyasaTheme.spacing.s))
                        Text(
                            text = profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!profile.location.isNullOrBlank()) {
                        Spacer(Modifier.height(NyasaTheme.spacing.s))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(NyasaTheme.spacing.xs))
                            Text(
                                text = profile.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (!profile.website.isNullOrBlank()) {
                        Spacer(Modifier.height(NyasaTheme.spacing.s))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Language,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(NyasaTheme.spacing.xs))
                            Text(
                                text = profile.website,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    val socialLinks = listOfNotNull(
                        profile.twitter?.let { "Twitter: $it" },
                        profile.facebook?.let { "Facebook: $it" },
                        profile.instagram?.let { "Instagram: $it" },
                        profile.linkedin?.let { "LinkedIn: $it" }
                    )
                    if (socialLinks.isNotEmpty()) {
                        Spacer(Modifier.height(NyasaTheme.spacing.m))
                        socialLinks.forEach { link ->
                            Text(
                                text = link,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(NyasaTheme.spacing.xs))
                        }
                    }
                }
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun AuthorProfileScreenPreview() {
    NyasaTheme {
        AuthorProfileScreen(
            profile = UserProfile(
                username = "Mphatso K.",
                bio = "Storyteller from the warm heart of Africa.",
                location = "Lilongwe, Malawi",
                website = "https://nyasablog.com",
                twitter = "@mphatso",
                facebook = null,
                instagram = "mphatso_k",
                linkedin = null,
                profileImage = null
            ),
            isLoading = false,
            onNavigateBack = {}
        )
    }
}
