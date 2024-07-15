package com.fol.com.fol.network

import kotlinx.coroutines.flow.StateFlow

//interface RTNManager {
//    fun startListening()
//    fun stopListening()
//
//    val connected : StateFlow<Boolean>
//    val messages : StateFlow<RtnEvent<*>?>
//}
//
//
//enum class EventType(val value: String) {
//    @Json(name = "likes") LIKE("likes"),
//    @Json(name = "likesPlus") SUPER_LIKE("likesPlus"),
//    @Json(name = "message") MESSAGE("message");
//
//    companion object {
//        fun fromValue(value: String): EventType? = entries.find { it.value == value }
//    }
//}
//
//interface RtnMessage<T> {
//    val eventType: EventType
//    val correlationId: String?
//    val eventTimeUTC: Date
//    val data: T
//}
//
//data class SuperLikeData(
//    val senderUserId: String,
//    val recipientUserId: String,
//    val causedMutual: Boolean
//)
//
//data class LikeData(
//    val senderUserId: String,
//    val recipientUserId: String,
//    val causedMutual: Boolean
//)
//
//data class MessageData(
//    val senderUserId: String,
//    val senderFirstName: String,
//    val senderPhotoUri: String,
//    val messageId: String,
//    val messageText: String,
//    val isMutual: Boolean
//)
//
//sealed class RtnEvent<T> : RtnMessage<T> {
//    data class SuperLikeEvent(
//        override val eventType: EventType,
//        override val correlationId: String?,
//        override val eventTimeUTC: Date,
//        override val data: SuperLikeData
//    ) : RtnEvent<SuperLikeData>()
//
//    data class LikeEvent(
//        override val eventType: EventType,
//        override val correlationId: String?,
//        override val eventTimeUTC: Date,
//        override val data: LikeData
//    ) : RtnEvent<LikeData>()
//
//    data class MessageEvent(
//        override val eventType: EventType,
//        override val correlationId: String?,
//        override val eventTimeUTC: Date,
//        override val data: MessageData
//    ) : RtnEvent<MessageData>()
//
//}
