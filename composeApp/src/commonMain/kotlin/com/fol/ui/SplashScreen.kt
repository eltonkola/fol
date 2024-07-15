package com.fol.com.fol.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.crypto.CryptoManager
import com.fol.com.fol.model.AppsScreen
import com.fol.com.fol.ui.elements.KeyboardUi
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = viewModel { SplashViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState.state) {
        is SplashOpState.Loading -> LoadingScreen()
        is SplashOpState.Pin -> PinScreen(viewModel)
        is SplashOpState.Error -> ErrorScreen()
        is SplashOpState.Ready -> {
            navController.navigate(AppsScreen.Main.name)
        }

        is SplashOpState.NoAccount -> {
            navController.navigate(AppsScreen.Landing.name)
        }

        SplashOpState.PinError -> PinErrorScreen(
            nuke = { viewModel.nuke() },
            onDismiss = {
                viewModel.retryPin()
            }
        )
    }
}


@Composable
fun PinErrorScreen(onDismiss:() -> Unit, nuke:() -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Wrong pin") },
        text = { Text("Without the right pin, you can't use the app!") },
        confirmButton = {


            Button(
                onClick = {
                    nuke()
                    onDismiss()
                }
            ) {
                Text("NUKE IT!")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Retry")
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
private fun ErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Fol cant work right now!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinScreen(
    viewModel: SplashViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Enter Pin") },
                actions = {

                    val coroutineScope = rememberCoroutineScope()

                    IconButton(onClick = {
                        coroutineScope.launch {
                            CryptoManager.testValidity()
                        }
                    }){
                        Icon(Icons.Default.Key, contentDescription = "Settings")
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
                    text = "To use the app, enter the secret code.",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))


                TextField(
                    value = uiState.pin,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.pin.length < 6) {
                    Text(
                        text = "Pin needs to be 6 chars: ${uiState.pin.length}/6",
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                KeyboardUi(
                    query = uiState.pin,
                    onQueryChange = { viewModel.updatePin(it) },
                    onDone = {
                        if (uiState.pin.length <= 6) {
                            viewModel.authenticateUser()
                        }
                    }
                )

            }
        }
    )

}
