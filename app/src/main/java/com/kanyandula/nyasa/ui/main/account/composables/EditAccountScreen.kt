package com.kanyandula.nyasa.ui.main.account.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.main.account.state.ProfileFormData
import com.kanyandula.nyasa.ui.theme.NyasaTheme

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
    onSave: (ProfileFormData) -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var username by rememberSaveable { mutableStateOf(initialUsername) }
    var bio by rememberSaveable { mutableStateOf(initialBio) }
    var location by rememberSaveable { mutableStateOf(initialLocation) }
    var website by rememberSaveable { mutableStateOf(initialWebsite) }
    var twitter by rememberSaveable { mutableStateOf(initialTwitter) }
    var facebook by rememberSaveable { mutableStateOf(initialFacebook) }
    var instagram by rememberSaveable { mutableStateOf(initialInstagram) }
    var linkedin by rememberSaveable { mutableStateOf(initialLinkedin) }

    fun save() {
        onSave(
            ProfileFormData(
                email, username, bio, location,
                website, twitter, facebook, instagram, linkedin
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
                        IconButton(
                            onClick = { save() },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Save"
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
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                NyasaTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username",
                    leadingIcon = Icons.Filled.Person,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "About You",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))

                NyasaTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio",
                    singleLine = false,
                    maxLines = 4,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Location",
                    leadingIcon = Icons.Filled.LocationOn,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = "Website",
                    leadingIcon = Icons.Filled.Language,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Social Links",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))

                NyasaTextField(
                    value = twitter,
                    onValueChange = { twitter = it },
                    label = "Twitter",
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = facebook,
                    onValueChange = { facebook = it },
                    label = "Facebook",
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = "Instagram",
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = linkedin,
                    onValueChange = { linkedin = it },
                    label = "LinkedIn",
                    imeAction = ImeAction.Done,
                    onImeAction = { save() }
                )
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
            initialEmail = "creator@nyasablog.mw",
            initialUsername = "nyasa_creator",
            initialBio = "Storyteller from the warm heart of Africa",
            initialLocation = "Lilongwe, Malawi",
            initialWebsite = "https://nyasablog.com",
            initialTwitter = "@nyasacreator",
            initialFacebook = "",
            initialInstagram = "nyasacreator",
            initialLinkedin = "",
            isLoading = false,
            onSave = {},
            onNavigateBack = {}
        )
    }
}
