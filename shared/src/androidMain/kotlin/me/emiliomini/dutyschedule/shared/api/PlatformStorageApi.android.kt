package me.emiliomini.dutyschedule.shared.api

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.IOException
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import me.emiliomini.dutyschedule.shared.api.adapter.ProtoAdapter
import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel
import me.emiliomini.dutyschedule.shared.services.storage.MultiplatformDataStore
import java.io.File

class AndroidStorageApi() : PlatformStorageApi {
    val logger = getPlatformLogger("AndroidStorageApi")

    val dataStores = mutableMapOf<String, DataStore<MultiplatformDataModel>>()

    override suspend fun initialize(stores: List<MultiplatformDataStore<out MultiplatformDataModel>>) {
        for (store in stores) {
            val serializer = ProtoAdapter(store.serializer as KSerializer<MultiplatformDataModel>)
            val dataStore = DataStoreFactory.create(
                serializer = serializer,
                corruptionHandler = ReplaceFileCorruptionHandler { store.defaultValue },
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = { File(APPLICATION_CONTEXT.filesDir, "${store.id}.pb") }
            )
            dataStores[store.id] = dataStore
        }
    }

    override suspend fun <T : MultiplatformDataModel> get(store: MultiplatformDataStore<T>): T? {
        if (!dataStores.containsKey(store.id)) {
            return null
        }

        try {
            @Suppress("UNCHECKED_CAST")
            return dataStores[store.id]?.data?.first() as T?
        } catch (e: NoSuchElementException) {
            logger.log("No data stored for datastore with id ${store.id}", throwable = e)
            return null
        } catch (e: ClassCastException) {
            logger.warn("Type mismatch for given datastore id ${store.id}", throwable = e)
            return null
        }
    }

    override suspend fun <T : MultiplatformDataModel> update(
        store: MultiplatformDataStore<T>,
        newData: T?
    ) {
        if (!dataStores.containsKey(store.id)) {
            logger.warn("Tried to persist value in nonexistent datastore with id ${store.id}")
            return
        }

        try {
            dataStores[store.id]!!.updateData {
                if (newData != null) {
                    newData as MultiplatformDataModel
                } else {
                    store.defaultValue as MultiplatformDataModel
                }
            }
        } catch (e: IOException) {
            logger.warn(
                "Failed to persist value while writing in datastore with id ${store.id}",
                throwable = e
            )
        } catch (e: Exception) {
            logger.warn("Failed to persist value in datastore with id ${store.id}", throwable = e)
        }
    }
}

actual fun getPlatformStorageApi(): PlatformStorageApi {
    return AndroidStorageApi()
}