package me.emiliomini.dutyschedule.shared.services.storage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.emiliomini.dutyschedule.shared.datastores.MultiplatformDataModel

class ListViewModel<P : MultiplatformDataModel, I>(
    dataStore: MultiplatformDataStore<P>,
    private val mapper: (P) -> List<I>
) : ViewModel() {
    val flow: Flow<List<I>> = dataStore.flow.map {
        mapper(it)
    }
}

class MapViewModel<P : MultiplatformDataModel, I>(
    dataStore: MultiplatformDataStore<P>,
    private val mapper: (P) -> Map<String, I>
) : ViewModel() {
    val flow: Flow<Map<String, I>> = dataStore.flow.map {
        mapper(it)
    }
}
