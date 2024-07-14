package com.fol.com.fol.ui.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.fol.com.fol.model.AppsScreen
import com.fol.com.fol.model.ThreadPreview
import com.fol.ui.elements.Identicon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel { MainViewModel() },
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar (
                title = {
                    Text("FOL - ${uiState.user.id}")
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(AppsScreen.Profile.name)
                    }){
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        navController.navigate(AppsScreen.Settings.name)
                    }){
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(AppsScreen.AddContact.name)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column (
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            if(uiState.threads.isEmpty()){
                LoadingScreen()
            }else{
                MessagesScreen(uiState.threads){
                    navController.navigate(AppsScreen.Thread.name + "/${it.id}")
                }
            }

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
private fun MessagesScreen(threads: List<ThreadPreview>, onClick: (ThreadPreview) -> Unit ) {

    LazyColumn (modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        items(items = threads, key = { item -> item.id }) { thread ->
            ThreadRow(thread = thread, onClick = onClick)
        }
    }
}


@Composable
private fun ThreadRow(thread: ThreadPreview, onClick: (ThreadPreview) -> Unit ) {

    Row(
        modifier = Modifier.fillMaxWidth(1f).clickable { onClick(thread) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ){

//        Identicon(
//            address = thread.contact.publicKey,
//            modifier = Modifier.size(64.dp)
//        )

        Identicon(
            publicKeyHash = thread.contact.publicKey,
            modifier = Modifier.size(64.dp),
            userName = thread.contact.name
        )

        Column (
            modifier = Modifier.fillMaxWidth(1f)
        ){
            Text(thread.lastMessage.message)
            Text(thread.lastMessage.kur.toString())
            Text(thread.contact.name)
        }

    }

}
