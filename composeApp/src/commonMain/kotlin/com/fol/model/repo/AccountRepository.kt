package com.fol.model.repo

import com.fol.com.fol.db.model.AppProfile
import com.fol.com.fol.db.AppSettings
import com.fol.com.fol.db.DbManager

class AccountRepository(
    private val appSettings: AppSettings,
    private val dbManager: DbManager
) {


    val currentUser: AppProfile
        get() = userAccount!!

    private var userAccount: AppProfile? = null

    fun accountExists() : Boolean {
        return appSettings.accountExists()
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
            appSettings.setAccountExists(true)
        }
        return userAccount
    }

    fun recoverUser( publicKey : String, privateKey : String, pin: String) : AppProfile? {
        //TODO -  validate public and private key

        return createUser(publicKey, privateKey, pin)
    }

    fun deleteAccount() {
        dbManager.deleteAllData()
        appSettings.setAccountExists(false)
        userAccount = null
    }


}
