package com.fol.com.fol.ui.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen

@Composable
fun CreateAccountScreen(
    navController: NavHostController,
    viewModel: CreateAccountViewModel = viewModel { CreateAccountViewModel() },

) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState) {
        is CreateAccountUiState.Idle -> CreateUiScreen(navController, viewModel)
        is CreateAccountUiState.Creating -> LoadingScreen()
        is CreateAccountUiState.Error -> ErrorScreen(viewModel)
        is CreateAccountUiState.Created -> {
            navController.navigate(AppsScreen.Main.name)
        }
    }
}

@Composable
private fun CreateUiScreen(
    navController: NavHostController,
    viewModel: CreateAccountViewModel
) {
    Column (
        modifier = Modifier.padding(16.dp)
    ){
        Text("Create Account Screen")
        Spacer(modifier = Modifier.size(32.dp))

        Button(onClick = {
            viewModel.createAccount()
        }){
            Text("Create Account")
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
    viewModel: CreateAccountViewModel
) {
    Column (
        modifier = Modifier.fillMaxSize(),
    ) {
        Text("Error creating account!")

        Button(onClick = {
            viewModel.resetForm()
        }){
            Text("Retry")
        }
    }
}
