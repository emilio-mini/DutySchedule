package me.emiliomini.dutyschedule.services.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import me.emiliomini.dutyschedule.datastore.AlarmItemsProto

private val Context.store_preferences: DataStore<Preferences> by preferencesDataStore("user_data")
private val Context.store_alarm_items: DataStore<AlarmItemsProto> by dataStore(
    fileName = "alarm_proto.pb",
    serializer = AlarmProtoSerializer
)

object DataStores {
    lateinit var PREFERENCES: DataStore<Preferences>
    lateinit var ALARM_ITEMS: DataStore<AlarmItemsProto>

    fun initialize(context: Context) {
        if (!this::PREFERENCES.isInitialized) this.PREFERENCES = context.store_preferences
        if (!this::ALARM_ITEMS.isInitialized) this.ALARM_ITEMS = context.store_alarm_items
    }
}