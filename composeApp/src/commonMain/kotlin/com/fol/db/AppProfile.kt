package com.fol.com.fol.db

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class AppProfile : RealmObject {
    @PrimaryKey
    var id: String = INSTANCE_ID
    var privateKey: String = ""
    var publicKey: String = ""

    companion object {
        const val INSTANCE_ID = "user_profile"
    }
}