package com.fol.com.fol.ui.app.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.fol.com.fol.model.AppsScreen
import com.fol.ui.theme.backgroundDark

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel() }
) {

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is SettingsUiState.Loading -> LoadingScreen()
        is SettingsUiState.Error -> ErrorScreen()
        is SettingsUiState.Deleted -> {
            navController.navigate(AppsScreen.Splash.name) {
                popUpTo(AppsScreen.Splash.name) { inclusive = true }
            }
        }
        is SettingsUiState.Ready -> SettingsScreen(
            (uiState as SettingsUiState.Ready),
            navController,
            viewModel
        )
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
            Text("Can load your settings, probably the app is fucked!!!")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsScreen(
        data: SettingsUiState.Ready,
        navController: NavHostController,
        viewModel: SettingsViewModel) {

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            ) {

                Text(
                    text = "System theme: ${if (data.systemTheme) "On" else "Off"}",
                    fontSize = 20.sp
                )
                Switch(
                    checked = data.systemTheme,
                    onCheckedChange = {
                        viewModel.updateSystemTheme(it)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                if(!data.systemTheme) {
                    Text(
                        text = "Dark Mode: ${if (data.darkTheme) "On" else "Off"}",
                        fontSize = 20.sp
                    )
                    Switch(
                        checked = data.darkTheme,
                        onCheckedChange = {
                            viewModel.updateDarkTheme(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Export account",
                    fontSize = 20.sp
                )
                Text(
                    text = "Make sure you store the private and public key secretly and securely.",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))


                Text(
                    text = "Public Key",
                    fontSize = 12.sp
                )

                val toaster = rememberToasterState()
                Toaster(state = toaster)


                Row {
                    TextField(
                        value = "fol://${data.user.publicKey}",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 2,
                        textStyle = TextStyle(fontSize = 10.sp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton({
                        clipboardManager.setText(AnnotatedString(data.user.publicKey))
                        toaster.show("Public key copied!")
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }

                Text(
                    text = "Private Key",
                    fontSize = 12.sp
                )
                Row {
                    TextField(
                        value = "fol://${data.user.privateKey}",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 2,
                        textStyle = TextStyle(fontSize = 10.sp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton({
                        clipboardManager.setText(AnnotatedString(data.user.privateKey))
                        toaster.show("private key copied!")
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Danger zone",
                    fontSize = 20.sp
                )
                Text(
                    text = "This operation cant not be reversed, all data and profile will be deleted.",
                    fontSize = 16.sp
                )

                Button(
                    onClick = {
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("DELETE ACCOUNT")
                }

            }
        }
    )


}