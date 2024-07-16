package com.fol.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient

expect fun createSecureHttpClient(tokenProvider: () -> String): HttpClient
