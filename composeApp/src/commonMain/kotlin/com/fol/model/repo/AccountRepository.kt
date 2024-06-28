package com.fol.model.repo

import com.fol.com.fol.model.DiGraph
import com.fol.com.fol.model.UserAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class AccountRepository(
    private val coroutineScope: CoroutineScope
) {

    val currentUser: UserAccount
        get() = userAccount!!

    private var userAccount: UserAccount? = null

    suspend fun loadUser() : UserAccount? {
        //TODO - load user account from local storage
        delay(2_000)
        return userAccount
    }

    suspend fun createUser(name : String, publicKey : String, privateKey : String) : UserAccount? {
        //TODO -  create user, private key and public one and save it on storage
        delay(1_000)
        userAccount = UserAccount(name, publicKey, privateKey)
        return userAccount

    }

    suspend fun recoverUser(name : String, publicKey : String, privateKey : String) : UserAccount? {
        //TODO -  create user, private key and public one and save it on storage
        delay(1_000)
        userAccount = UserAccount(name, publicKey, privateKey)
        return userAccount

    }

}
