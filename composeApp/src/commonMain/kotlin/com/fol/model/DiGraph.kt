package com.fol.com.fol.model

import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.db.DbManager
import com.fol.com.fol.model.repo.ContactsRepository
import com.fol.com.fol.model.repo.MessageEventReceiver
import com.fol.com.fol.model.repo.MessagesRepository
import com.fol.com.fol.network.BearerTokenLoader
import com.fol.com.fol.network.FakeNetwrok
import com.fol.com.fol.network.NetworkManager
import com.fol.com.fol.network.NetworkOperations
import com.fol.com.fol.network.ServerMessage
import com.fol.model.repo.AccountRepository
import com.fol.network.createHttpClient
import com.fol.network.createSecureHttpClient
import com.russhwolf.settings.Settings
import io.realm.kotlin.internal.platform.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object DiGraph {

    private val appDispatcher = Dispatchers.Main.limitedParallelism(100)
    private var coroutineScope = CoroutineScope(appDispatcher)

    private val settings: Settings = Settings()
    private val dbManager: DbManager = DbManager()

    val contactsRepository: ContactsRepository by lazy {
        ContactsRepository(coroutineScope = coroutineScope, dbManager = dbManager)
    }
    val appSettings = AppSettings(settings)


    val networkManager : NetworkOperations by lazy {
        NetworkManager(createSecureHttpClient{
            runBlocking {
                BearerTokenLoader.provideToken(createHttpClient(), accountRepository)
            }
        }, messageEventReceiver = object : MessageEventReceiver {
            override fun addMessageFromServer(message: ServerMessage) {
                messagesRepository.addMessageFromServer(message)
            }

            override fun gotDeliveryFromServer(deliveredId: List<Int>) {
                messagesRepository.gotDeliveryFromServer(deliveredId)
            }
        })
    }

    val accountRepository: AccountRepository by lazy {
        AccountRepository(dbManager = dbManager, appSettings = appSettings)
    }
    val messagesRepository: MessagesRepository by lazy {
        MessagesRepository(
            contactsRepository = contactsRepository,
            coroutineScope = coroutineScope,
            dbManager = dbManager,
            networkManager = networkManager,
            accountRepository = accountRepository
        )
    }

}
