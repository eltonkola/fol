package com.fol.com.fol.network

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow


//class RTNManagerImpl(
//    private val okApi: OkAPI,
//    private val getUserProvider: () -> BaseUserProvider,
//    private val userEnvironmentManager: UserEnvironmentManager,
//    private val appLocaleManager: AppLocaleManager
//) : RTNManager {
//
//    override val connected = MutableStateFlow(false)
//    override val messages = MutableStateFlow<RtnEvent<*>?>(null)
//
//    private val client = OkHttpClient.Builder()
//        .readTimeout(0, TimeUnit.MILLISECONDS)
//        .build()
//
//
//    private var webSocket: WebSocket? = null
//
//    private fun enabled() : Boolean {
//        return !userEnvironmentManager.legacyEndpoint.value
//    }
//
//    private fun getWsUrl() : String {
//        //TODO - for local testing only:
////        return "ws://192.168.0.2:8080/ws"
//
//        return Constants.E2P_RTN_URL
//    }
//
//    override fun startListening() {
//        if(!enabled()){
//            Logger.d("ws not started, not in e2p environment")
//            return
//        }
//        val url = getWsUrl()
//        val requestBuilder = Request.Builder()
//
//        addHeaders(requestBuilder)
//        requestBuilder.url("$url?eventType=subscribed&eventType=likes&eventType=likesPlus&eventType=message")
//
//        val request = requestBuilder.build()
//        Logger.d("ws request: $request")
//        webSocket = client.newWebSocket(request,  RTNSocketListener(
//            onConnection = {
//                connected.value = it
//            },
//            onMessage = { event ->
//                messages.value = event
//            }
//        ))
//    }
//
//    override fun stopListening() {
//        if(!enabled()){
//            Logger.d("ws not ended, not in e2p environment")
//            return
//        }
//        client.dispatcher.executorService.shutdown()
//    }
//
//    private fun addHeaders(builder: Request.Builder) {
//        val cookieHeaders: Map<String, String>? = okApi.getCookieHeader()
//        builder
//            .header(
//                "X-OkCupid-Device-Id",
//                TextUtils.join("; ", OkAPI.getDeviceHeader())
//            )
//            .header("X-OkCupid-App", OkAPI.sAppName)
//            .header("X-OkCupid-Platform", "Android")
//            .header("X-OkCupid-Version", OkAPI.sVersionNumber)
//            .header("X-OkCupid-Locale", appLocaleManager.getSessionLocale().toLanguageTag())
//            .header("User-Agent", OkAPI.getMatchHeader())
//            .header("x-match-useragent", OkAPI.getMatchHeader())
//
//        val cookieHeader = cookieHeaders?.get("Cookie")
//        if (cookieHeader != null) {
//            val cookieSession = getSessionValue(cookieHeader)
//            val userId = getUserProvider().getLoggedInUserId()
//            val coolieVal = "authToken=$cookieSession;uid=$userId"
//            builder.addHeader("Cookie", coolieVal)
//        }
//    }
//
//    private fun getSessionValue(cookieString: String): String? {
//        val key = "session="
//        val parts = cookieString.split("; ")
//        for (part in parts) {
//            if (part.startsWith(key)) {
//                return part.substring(key.length)
//            }
//        }
//        return null
//    }
//
//}
