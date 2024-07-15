package com.fol.com.fol.db.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey

class AppContact : RealmObject , Author {
    @PrimaryKey
    var id: String = RealmUUID.random().toString()
    var name: String = ""
    var publicKey: String = ""
}