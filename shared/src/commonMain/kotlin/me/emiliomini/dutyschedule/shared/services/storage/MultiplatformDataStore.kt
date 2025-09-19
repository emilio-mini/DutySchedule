package me.emiliomini.dutyschedule.shared.services.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

class MultiplatformDataStore<T>(
    val id: String, var data: T?,
    val onUpdate: suspend (store: MultiplatformDataStore<T>, newData: T?) -> Unit,
    val serializer: KSerializer<T>,
    val defaultValue: T
) where T : @Serializable Any {

    suspend fun updateData(transform: (data: T?) -> T) {
        data = transform(data)
        onUpdate(this, data)
    }

    suspend fun clearData() {
        data = null
        onUpdate(this, null)
    }
}