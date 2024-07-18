package com.fol.com.fol.ui.app.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fol.com.fol.ui.elements.KeyboardUi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBox(
    onSendChatClickListener: (String) -> Unit,
    modifier: Modifier
) {
    var chatBoxValue by remember { mutableStateOf("") }
    Column (
        modifier = modifier
    ){

        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = chatBoxValue,
                onValueChange = {

                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                enabled = false,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(text = "Type something")
                }
            )
            Spacer(modifier = Modifier.size(16.dp))
            IconButton(
                onClick = {
                    val msg = chatBoxValue
                    if (msg.isBlank()) return@IconButton
                    onSendChatClickListener(chatBoxValue)
                    chatBoxValue = ""
                },
                modifier = Modifier.size(32.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
        }

        KeyboardUi(
            query = chatBoxValue,
            onQueryChange = {
                chatBoxValue = it
            },
            onDone = {
                onSendChatClickListener(chatBoxValue)
                chatBoxValue = ""
            },
            actionName = "Send",
            validInput = true,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )

    }
}

