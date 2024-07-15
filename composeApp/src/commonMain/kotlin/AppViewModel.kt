package com.fol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.network.NetworkManager
import kotlinx.coroutines.launch

class AppViewModel(
    private val networkManager: NetworkManager = DiGraph.networkManager
) : ViewModel() {

    init{
        viewModelScope.launch {
            networkManager.connect()
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            networkManager.disconnect()
        }
        super.onCleared()
    }
}
