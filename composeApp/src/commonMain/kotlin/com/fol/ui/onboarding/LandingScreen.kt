package com.fol.com.fol.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen

@Composable
fun LandingScreen(navController: NavHostController) {

    Column (
        modifier = Modifier.padding(16.dp)
    ){
        Text("Landing Screen")
        Spacer(modifier = Modifier.size(32.dp))

        Button(onClick = {
            navController.navigate(AppsScreen.CreateAccount.name)
        }){
            Text("Create Account")
        }

        Spacer(modifier = Modifier.size(32.dp))

        Button(onClick = {
            navController.navigate(AppsScreen.RecoverAccount.name)
        }){
            Text("Recover Account")
        }
    }
}
