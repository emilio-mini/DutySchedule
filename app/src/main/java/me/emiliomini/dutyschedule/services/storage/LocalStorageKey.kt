package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

enum class LocalStorageKey(val target: Preferences.Key<String>, val defaultValue: String? = null) {
    USERNAME(stringPreferencesKey("login.username")),
    PASSWORD(stringPreferencesKey("login.password")),
    ALARM_OFFSET(stringPreferencesKey("preferences.alarm_offset"), "90")
}