package com.fol.com.fol.ui.app.thread

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person4
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.db.model.AppProfile
import com.fol.com.fol.model.Message


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    navController: NavHostController,
    userId: String,
    viewModel: ThreadViewModel = viewModel { ThreadViewModel(userId) },
    ) {

    val uiState by viewModel.uiState.collectAsState()


    Scaffold (
        topBar = {
            TopAppBar (
                title = {
                    Text(uiState.contact?.name ?: "...")
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
                        viewModel.showContact()
                    }){
                        Icon(Icons.Default.Person4, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        viewModel.onDeleteContact()
                    }){
                        Icon(Icons.Default.Delete, contentDescription = "Delete contact")
                    }
                },
            )
        },

    ) { innerPadding ->


        DeleteContactUi(viewModel, navController, uiState.contactStatus)

        ChatScreen(
            model = uiState,
            onSendChatClickListener = { msg -> viewModel.sendChat(msg) },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )

    }

}


@Composable
fun ChatScreen(
    model: ThreadUiState,
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
                ChatItem(item, model.user)
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

fun Message.isFromMe(user: AppProfile) : Boolean {
    return author == user
}

