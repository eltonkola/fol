package com.fol.com.fol.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen
import com.fol.com.fol.ui.elements.KeyboardUi

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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Fol cant work right now!")
    }
}

@Composable
private fun PinScreen(
    viewModel: SplashViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Pin") },
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "To use the app, enter the secret code.",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.pin.length < 6) {
                    Text("Pin needs to be 6 chars: ${uiState.pin.length}/6")
                }

                TextField(
                    value = uiState.pin,
                    onValueChange = {},
                    readOnly = true,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                KeyboardUi(
                    query = uiState.pin,
                    onQueryChange = { viewModel.updatePin(it) },
                    onDone = {
                        if (uiState.pin.length < 6) {
                            viewModel.authenticateUser()
                        }
                    }
                )

            }
        }
    )

}
