package me.emiliomini.dutyschedule.shared.services.storage

import kotlinx.serialization.KSerializer
import me.emiliomini.dutyschedule.shared.api.getPlatformStorageApi
import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel

class MultiplatformDataStore<T : MultiplatformDataModel>(
    val id: String,
    var data: T?,
    val onUpdate: suspend (store: MultiplatformDataStore<T>, newData: T?) -> Unit,
    val serializer: KSerializer<T>,
    val defaultValue: T
) {
    private val storageApi = getPlatformStorageApi()

    suspend fun get(): T? {
        if (data == null) {
            data = storageApi.get(this)
        }

        return data
    }

    suspend fun update(transform: (data: T?) -> T) {
        data = transform(data)
        onUpdate(this, data)
    }

    suspend fun clear() {
        data = null
        onUpdate(this, null)
    }
}