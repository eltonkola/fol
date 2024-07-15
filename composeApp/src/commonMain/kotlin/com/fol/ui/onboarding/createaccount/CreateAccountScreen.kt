package com.fol.com.fol.ui.onboarding.createaccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.fol.com.fol.model.AppsScreen
import com.fol.com.fol.ui.elements.KeyboardUi

@Composable
fun CreateAccountScreen(
    navController: NavHostController,
    viewModel: CreateAccountViewModel = viewModel { CreateAccountViewModel() },

    ) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState.state) {
        is CreationState.Idle -> CreateUiScreen(navController, viewModel)
        is CreationState.Creating -> LoadingScreen()
        is CreationState.Error -> ErrorScreen(viewModel)
        is CreationState.Created -> {
            navController.navigate(AppsScreen.Main.name)
        }
    }
}


@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(
    viewModel: CreateAccountViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Error creating account!")

        Button(onClick = {
            viewModel.resetForm()
        }) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUiScreen(
    navController: NavHostController,
    viewModel: CreateAccountViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val toaster = rememberToasterState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            Toaster(state = toaster)
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
            ) {

                Text("Create a new account, by generating a new pair of keys and a password!")
                TextField(
                    value = uiState.privateKey,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 3,
                    textStyle = TextStyle(fontSize = 10.sp),
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Private key")
                    },
                    trailingIcon = {
                        IconButton({
                            clipboardManager.setText(AnnotatedString(uiState.privateKey))
                            toaster.show("Private key copied!")
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy"
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = uiState.publicKey,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 3,
                    textStyle = TextStyle(fontSize = 10.sp),
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Public key")
                    },
                    trailingIcon = {
                        IconButton({
                            clipboardManager.setText(AnnotatedString(uiState.publicKey))
                            toaster.show("Public key copied!")
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy"
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                    Button(onClick = {
                        viewModel.generateKey()
                    }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Text("Generate a new pair of keys")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextField(
                    value = uiState.pin,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Password needs to be $PASSWORD_LENGTH chars: ${uiState.pin.length}/$PASSWORD_LENGTH")
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                KeyboardUi(
                    modifier = Modifier.fillMaxWidth(),
                    query = uiState.pin,
                    onQueryChange = { viewModel.updatePin(it) },
                    onDone = {
                        viewModel.createAccount()
                    },
                    actionName = "Create",
                    validInput = uiState.pin.length >= PASSWORD_LENGTH
                )

            }
        }
    )

}

const val PASSWORD_LENGTH = 6