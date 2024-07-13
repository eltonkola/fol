package com.fol.com.fol.ui.app.addcontact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.fol.com.fol.ui.elements.KeyboardUi

@Composable
fun AddContactScreen(
    navController: NavHostController,
    viewModel: AddContactViewModel = viewModel { AddContactViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is AddContactUiState.Loading -> LoadingScreen()
        is AddContactUiState.Error -> ErrorScreen()
        is AddContactUiState.Ready -> AddContactScreen(
            (uiState as AddContactUiState.Ready),
            viewModel,
            navController
        )
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
private fun ErrorScreen() {
    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Error creating!!!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactScreen(
    data : AddContactUiState.Ready,
    viewModel: AddContactViewModel,
    navController: NavHostController) {

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a new contact") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "You can add a contact by manually pasting their public key, or by scanning their QR code.",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Public code",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val toaster = rememberToasterState()
                Toaster(state = toaster)
                if(data.error != null) {
                    Text(
                        text = data.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row {
                    TextField(
                        value = data.publicKey,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 2,
                        textStyle = TextStyle(fontSize = 10.sp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton({
                        clipboardManager.getText()?.let { viewModel.updatePublicKey(it.toString()) }
                        toaster.show("Public profile code pasted!")
                    }) {
                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Copy")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Or",
                    fontSize = 20.sp
                )

                Button({
                    toaster.show("TODO - implement qrcode reader")
                }) {
                    Text("Scan QR code")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = data.name,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                KeyboardUi(
                    query = data.name,
                    onQueryChange = { viewModel.updateName(it) },
                    onDone = {
                        if(data.name.isNotBlank() && data.publicKey.isNotBlank()) {
                            viewModel.addContact {
                                navController.popBackStack()
//                            navController.navigate("profile/${data.publicKey}")
                            }
                        }
                    }
                )

            }
        }
    )
}