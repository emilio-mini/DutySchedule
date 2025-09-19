package me.emiliomini.dutyschedule.shared.services.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

object MultiplatformNetworkAdapter {
    val HTTP: HttpClient = HttpClient {
        install(ContentEncoding) {
            gzip()
            deflate()
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpCookies) {
            storage = PersistentCookieStorage()
        }
    }
}