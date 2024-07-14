package com.fol.com.fol.ui.app.thread

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.fol.com.fol.db.AppContact
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
fun ContactProfileUi(
    viewModel: ThreadViewModel,
    contact: AppContact
) {

    AlertDialog(
        onDismissRequest = { viewModel.dismissDialog() },
        title = { Text(text = contact.name) },
        text = {
            ProfileUi(contact)
        },
        confirmButton = { },
        dismissButton = {
            Button(
                onClick = { viewModel.dismissDialog() }
            ) {
                Text("Ok")
            }
        }
    )
}

@Composable
private fun ProfileUi(
    contact: AppContact
) {
    Column(
        modifier = Modifier.width(360.dp).height(360.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Here is their public key, in case you want to securely share it with others:")

        val clipboardManager = LocalClipboardManager.current

        Spacer(modifier = Modifier.height(16.dp))

        val toaster = rememberToasterState()
        Row {
            TextField(
                value = "fol://${contact.publicKey}",
                onValueChange = {},
                readOnly = true,
                maxLines = 2,
                textStyle = TextStyle(fontSize = 10.sp),
                modifier = Modifier.weight(1f)
            )
            IconButton({
                clipboardManager.setText(AnnotatedString(contact.publicKey))
                toaster.show("Public profile code copied!")
            }) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
            }
        }


        Toaster(state = toaster)

        Spacer(modifier = Modifier.height(16.dp))

        val logoPainter : Painter = painterResource(Res.drawable.compose_multiplatform)
        Image(
            modifier = Modifier.size(320.dp).padding(16.dp),
            painter = rememberQrCodePainter("fol://${contact.publicKey}"){
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
            contentDescription = "QR code sharing contact's profile"
        )

        Spacer(modifier = Modifier.weight(1f))


    }
}
