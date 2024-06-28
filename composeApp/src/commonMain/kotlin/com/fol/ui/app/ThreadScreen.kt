package com.fol.com.fol.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun ThreadScreen(navController: NavHostController, userId: String) {

    Column {
        Text("Thread Screen - $userId")

        Button(onClick = {
            navController.popBackStack()
        }){
            Text("Back")
        }

    }

}