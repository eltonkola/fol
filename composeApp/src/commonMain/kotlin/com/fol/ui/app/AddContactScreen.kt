package com.fol.com.fol.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun AddContactScreen(navController: NavHostController) {
    Column {
        Text("Add Contact Screen")

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("Back")
        }
    }
}
