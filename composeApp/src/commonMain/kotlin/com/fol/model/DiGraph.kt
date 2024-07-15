package com.fol.com.fol.model

import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.db.DbManager
import com.fol.com.fol.model.repo.ContactsRepository
import com.fol.model.repo.AccountRepository
import com.fol.com.fol.model.repo.MessagesRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object DiGraph {

    private val appDispatcher = Dispatchers.Main.limitedParallelism(100)
    private var coroutineScope = CoroutineScope(appDispatcher)

    private val settings: Settings = Settings()
    val dbManager: DbManager = DbManager()
    val contactsRepository: ContactsRepository by lazy {
        ContactsRepository(coroutineScope = coroutineScope, dbManager = dbManager)
    }
    val appSettings = AppSettings(settings)

    val accountRepository: AccountRepository by lazy {
        AccountRepository(dbManager = dbManager, appSettings = appSettings)
    }
    val messagesRepository: MessagesRepository by lazy {
        MessagesRepository(contactsRepository = contactsRepository, coroutineScope = coroutineScope, dbManager = dbManager)
    }

}
