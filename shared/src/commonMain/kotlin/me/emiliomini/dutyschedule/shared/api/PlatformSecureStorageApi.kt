package me.emiliomini.dutyschedule.shared.api

/**
 * Key value store for secrets that must not end up in the plain protobuf files written by
 * [me.emiliomini.dutyschedule.shared.services.storage.StorageService]
 */
interface PlatformSecureStorageApi {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
    suspend fun remove(key: String)
}

expect fun initializePlatformSecureStorageApi(): PlatformSecureStorageApi

private var api: PlatformSecureStorageApi? = null

fun getPlatformSecureStorageApi(): PlatformSecureStorageApi = if (api == null) {
    api = initializePlatformSecureStorageApi()
    api!!
} else {
    api!!
}
