package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataKeys {
    val USERNAME = DataKeyEntry(stringPreferencesKey("login.username"))
    val PASSWORD = DataKeyEntry(stringPreferencesKey("login.password"))
    val ALARM_OFFSET = DataKeyEntry(longPreferencesKey("preferences.alarm_offset"), 90L)
}

data class DataKeyEntry<T>(val key: Preferences.Key<T>, val fallback: T? = null)