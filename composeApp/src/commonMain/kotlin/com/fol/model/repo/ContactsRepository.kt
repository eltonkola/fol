package com.fol.com.fol.model.repo

import co.touchlab.kermit.Logger
import com.fol.com.fol.db.AppContact
import com.fol.com.fol.db.DbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsRepository(
    private val coroutineScope: CoroutineScope,
    private val dbManager: DbManager
) {

    private val _allContacts = MutableStateFlow<List<AppContact>>(emptyList())
    val allContacts: StateFlow<List<AppContact>> get() = _allContacts

    init {
        coroutineScope.launch {
            dbManager.contacts()
                .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
                .collect { contacts ->
                    _allContacts.value = contacts
                    Logger.i { "contacts - loaded: ${contacts.size}" }
                    contacts.forEach { Logger.i { "contacts - ID: ${it.id} - Name: ${it.name} - PublicKey: ${it.publicKey}" } }
                }
        }
    }

    fun addContact(name: String, publicKey: String) : Boolean {
        Logger.i{ "addContact name: $name - publicKey: $publicKey" }
       return try{
            dbManager.addContact(name, publicKey)
           Logger.i{ "added" }
            true
        }catch (e: Exception){
            Logger.e("ContactsRepository", e, "Error adding contact")
            false
        }
    }

    fun getContactById(userId: String): AppContact? {
        return dbManager.getContactById(userId)
    }

    fun deleteContact(contact: AppContact) {
        return dbManager.deleteContactById(contact.id)
    }

}
