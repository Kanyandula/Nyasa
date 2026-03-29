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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(
    initialEmail: String,
    initialUsername: String,
    isLoading: Boolean,
    onSave: (email: String, username: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var username by rememberSaveable { mutableStateOf(initialUsername) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Edit Profile",
                    onNavigationClick = onNavigateBack,
                    actions = {
                        IconButton(
                            onClick = { onSave(email, username) },
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
                    imeAction = ImeAction.Done,
                    onImeAction = { onSave(email, username) }
                )
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
            isLoading = false,
            onSave = { _, _ -> },
            onNavigateBack = {}
        )
    }
}
