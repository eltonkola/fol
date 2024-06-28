package com.fol.com.fol.model

import com.fol.com.fol.model.repo.ContactsRepository
import com.fol.model.repo.AccountRepository
import com.fol.com.fol.model.repo.MessagesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object DiGraph{

    private val appDispatcher = Dispatchers.Main.limitedParallelism(100)
    private val coroutineScope = CoroutineScope(appDispatcher)

    val accountRepository = AccountRepository(coroutineScope = coroutineScope)
    val contactsRepository = ContactsRepository(coroutineScope = coroutineScope)
    val messagesRepository = MessagesRepository(contactsRepository = contactsRepository, coroutineScope = coroutineScope)

}
