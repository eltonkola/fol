package com.fol.com.fol.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.fol.com.fol.model.AppsScreen
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun ThreadScreen(
    navController: NavHostController,
    userId: String,
    viewModel: ThreadViewModel = viewModel { ThreadViewModel() },
    ) {

    Scaffold(
        topBar = {
            TopAppBar (
                backgroundColor = MaterialTheme.colors.primarySurface,
                contentColor = MaterialTheme.colors.onPrimary,
                title = {
                    Text("Thread Screen - $userId")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }){
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(AppsScreen.Profile.name)
                    }){
//                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },

    ) { innerPadding ->

        val conversation = viewModel.conversation.collectAsState()

        ChatScreen(
            model = ChatUiModel(
                messages = conversation.value,
                addressee = ChatUiModel.Author.bot
            ),
            onSendChatClickListener = { msg -> viewModel.sendChat(msg) },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )

    }

}


data class ChatUiModel(
    val messages: List<Message>,
    val addressee: Author,
) {
    data class Message(
        val text: String,
        val author: Author,
    ) {
        val isFromMe: Boolean
            get() = author.id == MY_ID

        companion object {
            val initConv = Message(
                text = "Hi there, how you doing?",
                author = Author.bot
            )
        }
    }

    data class Author(
        val id: String,
        val name: String
    ) {
        companion object {
            val bot = Author("1", "Bot")
            val me = Author(MY_ID, "Me")
        }
    }

    companion object {
        const val MY_ID = "-1"
    }
}

@Composable
fun ChatScreen(
    model: ChatUiModel,
    onSendChatClickListener: (String) -> Unit,
    modifier: Modifier
) {
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (messages, chatBox) = createRefs()

        val listState = rememberLazyListState()
        LaunchedEffect(model.messages.size) {
            listState.animateScrollToItem(model.messages.size)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(messages) {
                    top.linkTo(parent.top)
                    bottom.linkTo(chatBox.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                },
            contentPadding = PaddingValues(16.dp)
        ) {
            items(model.messages) { item ->
                ChatItem(item)
            }
        }
        ChatBox(
            onSendChatClickListener,
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(chatBox) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@Composable
fun ChatItem(message: ChatUiModel.Message) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)) {
        Box(
            modifier = Modifier
                .align(if (message.isFromMe) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 48f,
                        topEnd = 48f,
                        bottomStart = if (message.isFromMe) 48f else 0f,
                        bottomEnd = if (message.isFromMe) 0f else 48f
                    )
                )
                .background(MaterialTheme.colors.primary)
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                color = MaterialTheme.colors.onPrimary,
            )
        }
    }
}

@Composable
fun ChatBox(
    onSendChatClickListener: (String) -> Unit,
    modifier: Modifier
) {
    var chatBoxValue by remember { mutableStateOf(TextFieldValue("")) }
    Row(modifier = modifier.padding(16.dp)) {
        TextField(
            value = chatBoxValue,
            onValueChange = { newText ->
                chatBoxValue = newText
            },
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
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
                val msg = chatBoxValue.text
                if (msg.isBlank()) return@IconButton
                onSendChatClickListener(chatBoxValue.text)
                chatBoxValue = TextFieldValue("")
            },
            modifier = Modifier.size(32.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.primary)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }
    }
}

