package com.fol.com.fol.ui.onboarding.recoveraccount

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fol.com.fol.model.AppsScreen

@Composable
fun RecoverAccountScreen(
    navController: NavHostController,
    viewModel: RecoverAccountViewModel = viewModel { RecoverAccountViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState) {
        is RecoverAccountUiState.Idle -> RecoverUiScreen(navController, viewModel)
        is RecoverAccountUiState.Creating -> LoadingScreen()
        is RecoverAccountUiState.Error -> ErrorScreen(viewModel)
        is RecoverAccountUiState.Recovered -> {
            navController.navigate(AppsScreen.Main.name)
        }
    }
}

@Composable
private fun RecoverUiScreen(
    navController: NavHostController,
    viewModel: RecoverAccountViewModel
) {
    Column (
        modifier = Modifier.padding(16.dp)
    ){
        Text("Recover Account Screen")
        Spacer(modifier = Modifier.size(32.dp))

        Button(onClick = {
            viewModel.recoverAccount()
        }){
            Text("Recover Account")
        }

        Button(onClick = {
            navController.popBackStack()
        }){
            Text("Back")
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
