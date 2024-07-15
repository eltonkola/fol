package com.fol.com.fol.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.fol.com.fol.model.AppsScreen
import fol.composeapp.generated.resources.Res
import fol.composeapp.generated.resources.landing_background
import fol.composeapp.generated.resources.screen_main
import fol.composeapp.generated.resources.terms_body
import fol.composeapp.generated.resources.terms_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LandingScreen(
    navController: NavHostController,
    viewModel: LandingScreenViewModel = viewModel { LandingScreenViewModel() },
) {

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showTerms || !uiState.acceptedTerms) {
        TOSScreen(
            onAccept = {
                viewModel.accept()
            },
            force =  !uiState.acceptedTerms
            )
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(Res.drawable.landing_background),
            contentDescription = "Fol Background",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = "FOL",
                fontSize = 64.sp
            )
            Text(
                text = "Fortress of Lines",
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Spacer(modifier = Modifier.size(16.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                onClick = {
                    navController.navigate(AppsScreen.CreateAccount.name)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Create, contentDescription = null)
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Button(onClick = {
                navController.navigate(AppsScreen.RecoverAccount.name)
            }) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.ImportExport, contentDescription = null)
                    Text("Recover Account")
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            TextButton({
                viewModel.showTerms()
            }){
                Text("Terms of service")
            }
        }
    }
}


@Composable
private fun TOSScreen(onAccept:() -> Unit, force: Boolean) {

    val toaster = rememberToasterState()
    Toaster(state = toaster)

    AlertDialog(
        onDismissRequest = {
            if(force){
                toaster.show("Close the app, and uninstall it you don't agree to these terms!")
            }else{
                onAccept()
            }
        },
        title = { Text(text = stringResource(Res.string.terms_title)) },
        text = {

            Box(modifier = Modifier
                .fillMaxHeight(1f)
                .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(Res.string.terms_body))
            }

        },
        confirmButton = {
            Button(
                onClick = {
                    onAccept()
                }
            ) {
                if(force){
                    Text("Accept")
                }else{
                    Text("Cool")
                }

            }
        },
        dismissButton = {
            if(force) {
                Button(
                    onClick = {
                        toaster.show("Close the app, and uninstall it you don't agree to these terms!")
                    }
                ) {
                    Text("Don't accept")
                }
            }
        }
    )
}
