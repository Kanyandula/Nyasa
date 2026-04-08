package com.kanyandula.nyasa.ui.main.account.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.components.ProfileAvatar
import com.kanyandula.nyasa.ui.main.account.state.ProfileFormData
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    initialEmail: String,
    initialUsername: String,
    initialBio: String,
    initialLocation: String,
    initialWebsite: String,
    initialTwitter: String,
    initialFacebook: String,
    initialInstagram: String,
    initialLinkedin: String,
    isLoading: Boolean,
    profileImage: String? = null,
    onSave: (ProfileFormData) -> Unit,
    onNavigateBack: () -> Unit,
    onDeleteAccount: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var username by rememberSaveable { mutableStateOf(initialUsername) }
    var bio by rememberSaveable { mutableStateOf(initialBio) }

    fun save() {
        onSave(
            ProfileFormData(
                email, username, bio, initialLocation,
                initialWebsite, initialTwitter, initialFacebook,
                initialInstagram, initialLinkedin
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Edit Profile",
                    onNavigationClick = onNavigateBack,
                    actions = {
                        Button(
                            onClick = { save() },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                ProfileAvatar(imageUrl = profileImage, size = 96.dp)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = username.ifEmpty { "Creator Name" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "@${username.ifEmpty { "username" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(28.dp))

                // Username field
                Text(
                    text = "USERNAME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                NyasaTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Your username",
                    leadingIcon = Icons.Filled.AlternateEmail,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "YOUR UNIQUE HANDLE ON NYASABLOG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Email field
                Text(
                    text = "EMAIL ADDRESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                NyasaTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "USED FOR ACCOUNT SECURITY AND UPDATES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Bio field
                Text(
                    text = "BIO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                NyasaTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Tell us about yourself",
                    singleLine = false,
                    maxLines = 4,
                    imeAction = ImeAction.Done,
                    onImeAction = { save() }
                )

                Spacer(Modifier.height(32.dp))

                // Delete Account
                TextButton(onClick = onDeleteAccount) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "  Delete Account",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun EditAccountScreenPreview() {
    NyasaTheme {
        EditAccountScreen(
            initialEmail = "creator@nyasablog.com",
            initialUsername = "nyasa_creator",
            initialBio = "Malawian storyteller capturing the ripples of the Lake through digital ink.",
            initialLocation = "Lilongwe, Malawi",
            initialWebsite = "https://nyasablog.com",
            initialTwitter = "@nyasacreator",
            initialFacebook = "",
            initialInstagram = "nyasacreator",
            initialLinkedin = "",
            isLoading = false,
            onSave = {},
            onNavigateBack = {},
            onDeleteAccount = {}
        )
    }
}
