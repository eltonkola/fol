package com.fol.com.fol.network

//
//import com.google.gson.JsonParser
//import com.squareup.moshi.FromJson
//import com.squareup.moshi.JsonAdapter
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.ToJson
//import okhttp3.Response
//import okhttp3.WebSocket
//import okhttp3.WebSocketListener
//import okio.ByteString
//import timber.log.Timber
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//
//class RTNSocketListener(
//    private val onConnection: (Boolean) -> Unit,
//    private val onMessage: (RtnEvent<*>) -> Unit
//) : WebSocketListener() {
//
//    override fun onOpen(webSocket: WebSocket, response: Response) {
//        onConnection(true)
//        Timber.d("WebSocket onOpen webSocket: $webSocket - response: $response")
//    }
//
//    override fun onMessage(webSocket: WebSocket, text: String) {
//        Timber.d("WebSocket Received message: $text")
//        parseMessage(text)
//    }
//
//    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//        Timber.d("WebSocket Received binary message: " + bytes.hex())
//        parseMessage(bytes.utf8())
//    }
//
//    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//        onConnection(false)
//        Timber.d("WebSocket onClosing webSocket: $webSocket - code: $code - reason: $reason")
//        webSocket.close(NORMAL_CLOSURE_STATUS, null)
//    }
//
//    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//        Timber.d("WebSocket closed: $reason - code: $code")
//    }
//
//    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//        Timber.d("WebSocket failure: " + t.message)
//        t.printStackTrace()
//    }
//
//    companion object {
//        const val NORMAL_CLOSURE_STATUS: Int = 1000
//    }
//
//    private val moshi: Moshi = Moshi.Builder()
//        .add(DateJsonAdapter())
//        .add(EventTypeAdapter())
//        .add(KotlinJsonAdapterFactory())
//        .build()
//
//    private fun parseMessage(message: String) {
//        val jsonObject = JsonParser.parseString(message).asJsonObject
//        val eventTypeString = jsonObject["eventType"].asString
//        val eventType = EventType.fromValue(eventTypeString)
//
//
//
//        when (eventType) {
//            EventType.LIKE -> {
//                val adapter = moshi.adapter(RtnEvent.LikeEvent::class.java)
//                val likeEvent = adapter.fromJson(message)
//                if(likeEvent != null) {
//                    onMessage(likeEvent)
//                }
//            }
//
//            EventType.SUPER_LIKE -> {
//                val adapter = moshi.adapter(RtnEvent.SuperLikeEvent::class.java)
//                val superLikeEvent = adapter.fromJson(message)
//                if(superLikeEvent != null) {
//                    onMessage(superLikeEvent)
//                }
//            }
//
//            EventType.MESSAGE -> {
//                val adapter = moshi.adapter(RtnEvent.MessageEvent::class.java)
//                val messageEvent = adapter.fromJson(message)
//                if(messageEvent != null) {
//                    onMessage(messageEvent)
//                }
//            }
//
//            else -> {
//                Timber.e("Unknown event type: $eventTypeString")
//            }
//        }
//    }
//}
//
//class DateJsonAdapter : JsonAdapter<Date>() {
//    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
//        timeZone = TimeZone.getTimeZone("UTC")
//    }
//    @FromJson
//    override fun fromJson(reader: com.squareup.moshi.JsonReader): Date? {
//        return try {
//            dateFormat.parse(reader.nextString())
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    @ToJson
//    override fun toJson(writer: com.squareup.moshi.JsonWriter, value: Date?) {
//        writer.value(value?.let { dateFormat.format(it) })
//    }
//}
//
//class EventTypeAdapter {
//    @FromJson
//    fun fromJson(value: String): EventType {
//        return EventType.fromValue(value) ?: throw IllegalArgumentException("Unexpected value: $value")
//    }
//
//    @ToJson
//    fun toJson(eventType: EventType): String {
//        return eventType.value
//    }
//}
