package com.fol.com.fol.ui.app.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fol.com.fol.db.model.AppProfile
import com.fol.com.fol.model.Message

@Composable
fun ChatItem(message: Message, user: AppProfile) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)) {
        Row(
            modifier = Modifier
                .align(if (message.isFromMe(user)) Alignment.End else Alignment.Start)

        ) {

            if(message.isFromMe(user)) {
                if (message.sent) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Sent")
                }
                if (message.delivered) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Delivered")
                }
            }


            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 48f,
                            topEnd = 48f,
                            bottomStart = if (message.isFromMe(user)) 48f else 0f,
                            bottomEnd = if (message.isFromMe(user)) 0f else 48f
                        )
                    )
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp)
            ) {
                Text(
                    text = message.message,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }


            if(!message.isFromMe(user)) {
                if (message.sent) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Sent")
                }
                if (message.delivered) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Delivered")
                }
            }


        }

    }
}
