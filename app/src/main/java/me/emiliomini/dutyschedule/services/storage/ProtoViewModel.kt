package me.emiliomini.dutyschedule.services.storage

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProtoListViewModel<P, I>(
    dataStore: DataStore<P>,
    private val mapper: (P) -> List<I>
) : ViewModel() {
    val flow: Flow<List<I>> = dataStore.data.map {
        mapper(it)
    }
}

class ProtoListViewModelFactory<P, I>(
    private val dataStore: DataStore<P>,
    private val mapper: (P) -> List<I>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProtoListViewModel(dataStore, mapper) as T
    }
}

class ProtoMapViewModel<P, I>(
    dataStore: DataStore<P>,
    private val mapper: (P) -> Map<String, I>
) : ViewModel() {
    val flow: Flow<Map<String, I>> = dataStore.data.map {
        mapper(it)
    }
}

class ProtoMapViewModelFactory<P, I>(
    private val dataStore: DataStore<P>,
    private val mapper: (P) -> Map<String, I>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProtoMapViewModel(dataStore, mapper) as T
    }
}