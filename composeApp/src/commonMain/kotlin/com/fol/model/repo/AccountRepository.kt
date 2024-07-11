package com.fol.model.repo

import com.fol.com.fol.db.AppProfile
import com.fol.com.fol.db.DbManager
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class AccountRepository(
    private val coroutineScope: CoroutineScope,
    private val settings: Settings,
    private val dbManager: DbManager
) {

    companion object {
        const val USER_CREATED = "USER_CREATED"
    }

    val currentUser: AppProfile
        get() = userAccount!!

    private var userAccount: AppProfile? = null

    fun accountExists() : Boolean {
        return settings.getBoolean(USER_CREATED, false)
    }

    fun loadUser(pin: String) : AppProfile? {
        val profile = dbManager.loadProfile(pin)
        userAccount = profile
        return userAccount
    }

    fun createUser( publicKey : String, privateKey : String, pin: String) : AppProfile? {
        val profile = dbManager.createProfile(passcode = pin, publicKey = publicKey, privateKey = privateKey)
        userAccount = profile
        if(userAccount != null){
            settings.putBoolean(USER_CREATED, true)
        }
        return userAccount
    }

    fun recoverUser( publicKey : String, privateKey : String, pin: String) : AppProfile? {
        //TODO -  validate public and private key

        return createUser(publicKey, privateKey, pin)
    }

}
