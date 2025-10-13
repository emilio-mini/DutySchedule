package me.emiliomini.dutyschedule.shared.services.network

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.coroutines.sync.withLock
import me.emiliomini.dutyschedule.shared.datastores.ClientCookies
import me.emiliomini.dutyschedule.shared.datastores.StoredCookie
import me.emiliomini.dutyschedule.shared.datastores.StoredCookieItems
import me.emiliomini.dutyschedule.shared.debug.DebugFlags
import me.emiliomini.dutyschedule.shared.services.storage.StorageService

class PersistentCookieStorage() : CookiesStorage {
    private var cache: MutableMap<String, MutableList<Cookie>>? = null
    private val mutex = kotlinx.coroutines.sync.Mutex()

    private suspend fun loadIfNeeded() = mutex.withLock {
        if (DebugFlags.DISABLE_COOKIE_PERSISTENCE.active()) {
            if (cache == null) {
                cache = mutableMapOf()
            }

            return@withLock
        }

        if (cache != null) return@withLock
        val cached = StorageService.COOKIES.getOrDefault().clientCookies
        if (cached.isEmpty()) {
            cache = mutableMapOf()
        } else {
            cache = cached.mapValues { (_, cookies) ->
                cookies.cookies.map { StoredCookie.toCookie(it) }.toMutableList()
            }.toMutableMap()
        }
    }

    private suspend fun persist() = mutex.withLock {
        StorageService.COOKIES.update {
            ClientCookies(
                cache?.mapValues { (_, cookies) ->
                    StoredCookieItems(cookies.map { StoredCookie.fromCookie(it) })
                } ?: emptyMap()
            )
        }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        loadIfNeeded()
        mutex.withLock {
            val host = requestUrl.host
            val list = cache!!.getOrPut(host) { mutableListOf() }
            list.removeAll { it.name == cookie.name }
            list.add(cookie)
        }
        persist()
    }

    override suspend fun get(requestUrl: Url): List<Cookie> {
        loadIfNeeded()
        return mutex.withLock {
            cache!![requestUrl.host]?.toList().orEmpty()
        }
    }

    override fun close() {}
}