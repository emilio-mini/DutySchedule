package me.emiliomini.dutyschedule.shared.services.storage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import me.emiliomini.dutyschedule.shared.api.getPlatformStorageApi
import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel

class MultiplatformDataStore<T : MultiplatformDataModel>(
    val id: String,
    val onUpdate: suspend (store: MultiplatformDataStore<T>, newData: T?) -> Unit,
    val serializer: KSerializer<T>,
    val defaultValue: T
) {
    private val mutex = Mutex()

    private val dataFlow = MutableStateFlow(defaultValue)
    private val storageApi = getPlatformStorageApi()
    private var isLoadedFromStorage = false

    val flow = dataFlow.asStateFlow()

    suspend fun getOrDefault(): T {
        ensureLoaded()
        return flow.value
    }

    suspend fun get(): T? {
        ensureLoaded()
        val result = flow.value
        return if (result == defaultValue) null else result
    }

    suspend fun update(transform: (data: T) -> T) {
        mutex.withLock {
            val initial = flow.value
            val result = transform(initial)
            if (result == initial) {
                return
            }
            dataFlow.update { result }
            onUpdate(this, result)
        }
    }

    fun isDefault(value: T): Boolean {
        return value == defaultValue
    }

    suspend fun clear() {
        dataFlow.update { defaultValue }
        onUpdate(this, null)
    }

    @Composable
    fun collectAsState(): State<T> = flow.collectAsState()

    private suspend fun ensureLoaded() {
        if (isLoadedFromStorage || flow.value != defaultValue) {
            return
        }

        mutex.withLock {
            if (isLoadedFromStorage) {
                return@withLock
            }

            val result = storageApi.get(this)
            if (result != null) {
                dataFlow.update { result }
            }
            isLoadedFromStorage = true
        }
    }
}