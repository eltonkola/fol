package com.fol.com.fol.ui.onboarding.recoveraccount

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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fol.com.fol.ui.onboarding.createaccount.PASSWORD_LENGTH

@Composable
fun RecoverAccountScreen(
    navController: NavHostController,
    viewModel: RecoverAccountViewModel = viewModel { RecoverAccountViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState.state) {
        is RecoveryState.Idle -> RecoverUiScreen(navController, viewModel)
        is RecoveryState.Recovering -> LoadingScreen()
        is RecoveryState.Error -> ErrorScreen(viewModel)
        is RecoveryState.Recovered -> {
            navController.navigate(AppsScreen.Main.name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoverUiScreen(
    navController: NavHostController,
    viewModel: RecoverAccountViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val toaster = rememberToasterState ()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recover Account") },
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

                Text("Import an existing account, by pasting your keys and entering a password!")
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
                            clipboardManager.getText()?.let { viewModel.updatePrivateKey(it.toString()) }
                            toaster.show("Private key pasted!")
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
                            clipboardManager.getText()?.let { viewModel.updatePublicKey(it.toString()) }
                            toaster.show("Public key pasted!")
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
                    if(uiState.invalidMessage != null) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null)
                        Text(uiState.invalidMessage!!, color = MaterialTheme.colorScheme.error)
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
                        viewModel.recoverAccount()
                    },
                    actionName = "Import",
                    validInput = uiState.pin.length >= PASSWORD_LENGTH
                )

            }
        }
    )
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
    viewModel: RecoverAccountViewModel
) {
    Column (
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Error recovering account!")

        Button(onClick = {
            viewModel.resetForm()
        }){
            Text("Retry")
        }
    }
}
