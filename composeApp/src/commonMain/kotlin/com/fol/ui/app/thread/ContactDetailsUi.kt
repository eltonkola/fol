package com.fol.com.fol.ui.app.thread

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen

@Composable
fun DeleteContactUi(
    viewModel: ThreadViewModel,
    navController: NavHostController,
    contactStatus: ContactStatus
) {

    val uiState by viewModel.uiState.collectAsState()

    when (contactStatus) {
        ContactStatus.Idle -> {}
        ContactStatus.ConformDelete -> {
            ConfirmationDialog(
                onConfirm = { viewModel.confirmDelete() },
                onDismiss = { viewModel.dismissDialog() }
            )
        }

        ContactStatus.Deleting -> {
            DeletingDialog()
        }

        ContactStatus.Deleted -> {
            navController.navigate(AppsScreen.Main.name) {
                popUpTo(AppsScreen.Main.name) { inclusive = true }
            }
        }

        ContactStatus.ErrorDeleting -> {
            ErrorDialog(onDismiss = { viewModel.dismissDialog() })
        }

        ContactStatus.ErrorLoading -> {
            ErrorLoadingProfileDialog(onDismiss = {
                navController.navigate(AppsScreen.Main.name) {
                    popUpTo(AppsScreen.Main.name) { inclusive = true }
                }
            })
        }

        ContactStatus.Details -> ContactProfileUi(viewModel, uiState.contact!!)
    }

}


@Composable
private fun ConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Delete Contact") },
        text = { Text("Are you sure you want to delete this contact?") },
        confirmButton = {
            Button(
                onClick = { onConfirm() }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeletingDialog() {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = "Deleting Contact") },
        text = { Text("Almost there..") },
        confirmButton = {
        },
        dismissButton = {
        }
    )
}

@Composable
private fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Error Deleting Contact") },
        text = { Text("For some reason we can delete the contact, please try again later!") },
        confirmButton = {
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Ok")
            }
        }
    )
}

@Composable
private fun ErrorLoadingProfileDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Error Loading Contact") },
        text = { Text("For some reason we can't load the contact and messages'!") },
        confirmButton = {
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Ok")
            }
        }
    )
}