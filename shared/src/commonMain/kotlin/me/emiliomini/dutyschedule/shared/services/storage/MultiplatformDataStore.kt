package me.emiliomini.dutyschedule.shared.services.storage

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val dataFlow = MutableStateFlow(data ?: defaultValue)
    private val storageApi = getPlatformStorageApi()

    val flow = dataFlow.asStateFlow()

    suspend fun getOrDefault(): T {
        if (data == null) {
            data = storageApi.get(this)
            dataFlow.update { data ?: defaultValue }
        }

        return data ?: defaultValue
    }

    suspend fun get(): T? {
        if (data == null) {
            data = storageApi.get(this)
            dataFlow.update { data ?: defaultValue }
        }

        return if (data == defaultValue) null else data
    }

    suspend fun update(transform: (data: T) -> T) {
        val result = transform(data ?: defaultValue)
        if (result == data) {
            return
        }
        data = result
        dataFlow.update { data ?: defaultValue }
        onUpdate(this, data)
    }

    fun isDefault(value: T): Boolean {
        return value == defaultValue
    }

    suspend fun clear() {
        data = null
        dataFlow.update { data ?: defaultValue }
        onUpdate(this, null)
    }
}