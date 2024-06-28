package com.fol.com.fol.model.repo

import com.fol.com.fol.model.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactsRepository(
    private val coroutineScope: CoroutineScope
) {

    private val _contacts = MutableStateFlow(emptyList<Contact>())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    init{
        coroutineScope.launch {
            loadContacts()
        }

    }

    suspend fun loadContacts(){
        delay(1_000)

        _contacts.update {
            it.toMutableList().apply { add(Contact("fake", "public")) }
        }
    }

    suspend fun createContact(name: String, publicKey: String) {
        delay(1_000)
        _contacts.update {
            it.toMutableList().apply { add(Contact(name, publicKey)) }
        }
    }

}
