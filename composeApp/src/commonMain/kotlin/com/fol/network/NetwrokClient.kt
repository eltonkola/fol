package com.fol.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
