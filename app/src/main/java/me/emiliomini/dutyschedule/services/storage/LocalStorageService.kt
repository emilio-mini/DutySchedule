package me.emiliomini.dutyschedule.services.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import me.emiliomini.dutyschedule.services.models.LocalStorageKey

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_data")

object LocalStorageService {
    private val localPreferences = mutableMapOf<Preferences.Key<String>, String>();

    suspend fun save(context: Context, key: LocalStorageKey, value: String) {
        this.localPreferences[key.target] = value;
        context.dataStore.edit { preferences ->
            preferences[key.target] = value
        }
    }

    suspend fun load(context: Context, key: LocalStorageKey): Result<String> {
        if (this.localPreferences.contains(key.target)) {
            return Result.success(this.localPreferences[key.target] ?: "");
        }

        context.dataStore.data.first().asMap().map { entry ->
            localPreferences[stringPreferencesKey(entry.key.name)] = entry.value as String;
        }

        val result = this.localPreferences[key.target];
        return if (result != null) {
            Result.success(result);
        } else {
            Result.failure(IOException("Key not found"));
        }
    }

    suspend fun loadValue(context: Context, key: LocalStorageKey): String? {
        return this.load(context, key).getOrNull();
    }

    suspend fun clear(context: Context, key: LocalStorageKey) {
        this.localPreferences.remove(key.target);
        context.dataStore.edit { preferences ->
            preferences.remove(key.target);
        }
    }
}
