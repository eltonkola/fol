package com.fol.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen

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
fun PinErrorScreen(onDismiss: () -> Unit, nuke: () -> Unit) {
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

