package me.emiliomini.dutyschedule.shared.services.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import io.ktor.http.Url
import io.ktor.http.takeFrom
import me.emiliomini.dutyschedule.shared.api.getPlatformConnectivityApi
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger

/**
 * Mirrors [HttpClient] API and checks network connectivity before sending requests; Methods return null when no connection is available
 */
object MultiplatformNetworkAdapter {
    private val connectivityApi = getPlatformConnectivityApi()
    private val HTTP: HttpClient = HttpClient {
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

    suspend fun get(
        url: Url,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse? = get {
        this.url.takeFrom(url)
        block()
    }

    suspend fun get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse? = get {
        url(urlString)
        block()
    }

    suspend fun get(block: HttpRequestBuilder.() -> Unit): HttpResponse? {
        return get(HttpRequestBuilder().apply(block))
    }

    suspend fun get(builder: HttpRequestBuilder): HttpResponse? {
        if (!connectivityApi.isConnected.value) {
            return null
        }

        return HTTP.get(builder)
    }

    suspend fun submitForm(
        url: String,
        formParameters: Parameters = Parameters.Empty,
        encodeInQuery: Boolean = false,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse? = submitForm(formParameters, encodeInQuery) {
        url(url)
        block()
    }

    suspend fun submitForm(
        formParameters: Parameters = Parameters.Empty,
        encodeInQuery: Boolean = false,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse? {
        if (!connectivityApi.isConnected.value) {
            return null
        }

        return HTTP.submitForm(
            formParameters, encodeInQuery, block
        )
    }

}