package com.fol.com.fol.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen
import com.fol.com.fol.ui.elements.KeyboardUi

@Composable
fun CreateAccountScreen(
    navController: NavHostController,
    viewModel: CreateAccountViewModel = viewModel { CreateAccountViewModel() },

) {
    val uiState by viewModel.uiState.collectAsState()
    when (uiState.state) {
        is CreationState.Idle -> CreateUiScreen(navController, viewModel)
        is CreationState.Creating -> LoadingScreen()
        is CreationState.Error -> ErrorScreen(viewModel)
        is CreationState.Created -> {
            navController.navigate(AppsScreen.Main.name)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateUiScreen(
    navController: NavHostController,
    viewModel: CreateAccountViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account Screen") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                Row {

                    Column {
                        TextField(
                            value = uiState.privateKey,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 3,
                            textStyle = TextStyle(fontSize = 10.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = uiState.publicKey,
                            onValueChange = {},
                            readOnly = true,
                            maxLines = 3,
                            textStyle = TextStyle(fontSize = 10.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(onClick = {
                        viewModel.generateKey()
                    }) {
                        Text("Regenerate keys")
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )

                }

                var isKeyboardOpened by remember { mutableStateOf(true) }

                if(uiState.pin.length < 6){
                    Text("Pin needs to be 6 chars: ${uiState.pin.length}/6")
                }


                Row {

                    TextField(
                        value = uiState.pin,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if(!isKeyboardOpened){
                        IconButton({
                            isKeyboardOpened = true
                        }){
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null
                            )
                        }
                    }

                }

                if(isKeyboardOpened){
                    Spacer(modifier = Modifier.height(32.dp))
                    KeyboardUi(
                        query = uiState.pin,
                        onQueryChange = { viewModel.updatePin(it) },
                        onDone = {
                            isKeyboardOpened = false
                        }
                    )

                }

                Row (
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Spacer(modifier = Modifier.width(16.dp))


                    Button(
                        enabled = uiState.pin.length == 6,
                        onClick = {
                        viewModel.createAccount()
                    }) {
                        Text("Create Account")
                    }

                }


            }
        }
    )

}