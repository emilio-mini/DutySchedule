package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataKeys {
    val USERNAME = DataKeyEntry(stringPreferencesKey("login.username"))
    val PASSWORD = DataKeyEntry(stringPreferencesKey("login.password"))
    val ALARM_OFFSET = DataKeyEntry(longPreferencesKey("preferences.alarm_offset"), 90L)
    val ALLOWED_ORGS = DataKeyEntry(stringPreferencesKey("prep.allowed_orgs"), "")
}

data class DataKeyEntry<T>(val key: Preferences.Key<T>, val fallback: T? = null)