package me.emiliomini.dutyschedule.services.storage.viewmodels

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.emiliomini.dutyschedule.datastore.AlarmItemsProto
import me.emiliomini.dutyschedule.datastore.AlarmProto

class AlarmListViewModel(
    private val dataStore: DataStore<AlarmItemsProto>
) : ViewModel() {
    val alarmsFlow: Flow<List<AlarmProto>> = dataStore.data.map {
        it.alarmsList
    }
}

class AlarmListViewModelFactory(
    private val dataStore: DataStore<AlarmItemsProto>
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmListViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
