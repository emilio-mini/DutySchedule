package me.emiliomini.dutyschedule.shared.services.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger

object MultiplatformNetworkAdapter {
    val HTTP: HttpClient = HttpClient {
        followRedirects = true

        install(ContentEncoding) {
            gzip()
            deflate()
        }

        install(Logging) {
            logger = object : Logger {
                val logger = getPlatformLogger("Ktor")

                override fun log(message: String) {
                    logger.d(message)
                }
            }
            level = LogLevel.INFO
        }

        install(HttpCookies) {
            storage = PersistentCookieStorage()
        }
    }
}