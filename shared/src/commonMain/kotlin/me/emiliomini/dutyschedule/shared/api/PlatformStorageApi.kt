package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel
import me.emiliomini.dutyschedule.shared.services.storage.MultiplatformDataStore

interface PlatformStorageApi {
    suspend fun initialize(stores: List<MultiplatformDataStore<out MultiplatformDataModel>>)
    suspend fun <T : MultiplatformDataModel> get(store: MultiplatformDataStore<T>): T?
    suspend fun <T : MultiplatformDataModel> update(store: MultiplatformDataStore<T>, newData: T?)
}

expect fun initializePlatformStorageApi(): PlatformStorageApi

private var api: PlatformStorageApi? = null

fun getPlatformStorageApi(): PlatformStorageApi = if (api == null) {
    api = initializePlatformStorageApi()
    api!!
} else {
    api!!
}
