package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.firstOrNull

object StorageService {
    const val DEFAULT_SEPARATOR = ";"

    suspend fun <T> save(entry: DataKeyEntry<T>, value: T) {
        DataStores.PREFERENCES.edit { preferences ->
            preferences[entry.key] = value
        }
    }

    suspend fun <T> save(store: DataStore<T>, value: T) {
        store.updateData { value }
    }

    suspend inline fun <reified T> load(entry: DataKeyEntry<T>): T? {
        val result = DataStores.PREFERENCES.data.firstOrNull()?.get(entry.key)
        if (result !is T) {
            return entry.fallback
        }

        return result ?: entry.fallback
    }

    suspend fun <T> load(store: DataStore<T>): T? {
        return store.data.firstOrNull()
    }

    suspend fun <T> clear(entry: DataKeyEntry<T>) {
        DataStores.PREFERENCES.edit { preferences ->
            preferences.remove(entry.key)
        }
    }
}
