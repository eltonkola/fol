package com.fol.com.fol.model

import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.db.DbManager
import com.fol.com.fol.model.repo.ContactsRepository
import com.fol.model.repo.AccountRepository
import com.fol.com.fol.model.repo.MessagesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object DiGraph{

    private val appDispatcher = Dispatchers.Main.limitedParallelism(100)
    private val coroutineScope = CoroutineScope(appDispatcher)

    private val settings: Settings = Settings()
    private val dbManager : DbManager = DbManager()
    private val contactsRepository = ContactsRepository(coroutineScope = coroutineScope)
    val appSettings = AppSettings(settings)

    val accountRepository = AccountRepository(dbManager = dbManager, appSettings = appSettings)
    val messagesRepository = MessagesRepository(contactsRepository = contactsRepository, coroutineScope = coroutineScope)

}
