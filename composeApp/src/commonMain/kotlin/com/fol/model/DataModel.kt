package com.fol.com.fol.model

import com.fol.com.fol.db.model.AppContact
import com.fol.com.fol.db.model.Author
import kotlinx.datetime.Instant

data class ThreadPreview(
    val id : String,
    val contact: AppContact,
    val lastMessage : Message,
)

data class Message(
    val message : String,
    val kur : Instant,
    val author: Author
)

