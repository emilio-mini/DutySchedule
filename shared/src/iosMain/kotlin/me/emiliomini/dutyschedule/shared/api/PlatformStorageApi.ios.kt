@file:OptIn(ExperimentalForeignApi::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.cinterop.ExperimentalForeignApi
import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel
import me.emiliomini.dutyschedule.shared.services.storage.MultiplatformDataStore

class IosStorageApi : PlatformStorageApi {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun initialize(stores: List<MultiplatformDataStore<out MultiplatformDataModel>>) {
        // TODO: Implement
    }

    override suspend fun <T : MultiplatformDataModel> get(
        store: MultiplatformDataStore<T>
    ): T? {
        // TODO: Implement
        return null
    }

    override suspend fun <T : MultiplatformDataModel> update(
        store: MultiplatformDataStore<T>, newData: T?
    ) {
        // TODO: Implement
    }

}

actual fun initializePlatformStorageApi(): PlatformStorageApi {
    return IosStorageApi()
}
