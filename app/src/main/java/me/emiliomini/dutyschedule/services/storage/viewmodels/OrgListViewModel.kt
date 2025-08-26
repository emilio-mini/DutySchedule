package me.emiliomini.dutyschedule.services.storage.viewmodels

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto

class OrgListViewModel(
    private val dataStore: DataStore<OrgItemsProto>
) : ViewModel() {
    val orgsFlow: Flow<List<OrgProto>> = dataStore.data.map {
        it.orgsList
    }
}

class OrgListViewModelFactory(
    private val dataStore: DataStore<OrgItemsProto>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrgListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrgListViewModel(dataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
