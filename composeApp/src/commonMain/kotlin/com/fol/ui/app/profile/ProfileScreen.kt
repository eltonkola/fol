package com.fol.com.fol.ui.app.profile

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.fol.com.fol.db.AppProfile
import fol.composeapp.generated.resources.Res
import fol.composeapp.generated.resources.compose_multiplatform
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel  = viewModel { ProfileViewModel() }
) {

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is ProfileUiState.Loading -> LoadingScreen()
        is ProfileUiState.Error -> ErrorScreen()
        is ProfileUiState.Ready -> SelfProfileScreen((uiState as ProfileUiState.Ready).user, navController)
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
        Text("Can load your profile, probably the app is fucked!!!")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelfProfileScreen(user: AppProfile,  navController: NavHostController) {

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your profile") },
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Securely share this profile with your friends. Send it via a secure channel, as text, or scan as a qr code below.",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val toaster = rememberToasterState()
                Row {
                    TextField(
                        value = "fol://${user.publicKey}",
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 2,
                        textStyle = TextStyle(fontSize = 10.sp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton({
                        clipboardManager.setText(AnnotatedString(user.publicKey))
                        toaster.show("Public profile code copied!")
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                }


                Toaster(state = toaster)

                Spacer(modifier = Modifier.height(16.dp))

                val logoPainter : Painter = painterResource(Res.drawable.compose_multiplatform)
                Image(
                    modifier = Modifier.padding(16.dp),
                    painter = rememberQrCodePainter("fol://${user.publicKey}"){
                        logo {
                            painter = logoPainter
                            padding = QrLogoPadding.Natural(.1f)
                            shape = QrLogoShape.circle()
                            size = 0.2f
                        }

                        shapes {
                            ball = QrBallShape.circle()
                            darkPixel = QrPixelShape.roundCorners()
                            frame = QrFrameShape.roundCorners(.25f)
                        }
                        colors {
                            dark = QrBrush.brush {
                                Brush.linearGradient(
                                    0f to Color.Red,
                                    1f to Color.Blue,
                                    end = Offset(it, it)
                                )
                            }
                            frame = QrBrush.solid(Color.Black)
                        }
                    },
                    contentDescription = "QR code sharing owners profile"
                )

                Spacer(modifier = Modifier.weight(1f))


            }
        }
    )
}

