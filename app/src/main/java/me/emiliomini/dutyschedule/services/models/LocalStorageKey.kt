package me.emiliomini.dutyschedule.services.models

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

enum class LocalStorageKey(val target: Preferences.Key<String>) {
    USERNAME(stringPreferencesKey("login.username")),
    PASSWORD(stringPreferencesKey("login.password")),
    ALARM_OFFSET(stringPreferencesKey("preferences.alarm_offset"))
}