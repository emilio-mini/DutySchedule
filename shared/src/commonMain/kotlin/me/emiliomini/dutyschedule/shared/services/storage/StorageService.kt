package me.emiliomini.dutyschedule.shared.services.storage

import me.emiliomini.dutyschedule.shared.api.getPlatformStorageApi
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.EmployeeItems
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.datastores.Statistics
import me.emiliomini.dutyschedule.shared.datastores.UpcomingDutyItems
import me.emiliomini.dutyschedule.shared.datastores.UserPreferences

object StorageService {

    object Stores {
        val USER_PREFERENCES = MultiplatformDataStore(
            "user_preferences",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            UserPreferences.serializer(),
            UserPreferences()
        )
        val STATISTICS = MultiplatformDataStore(
            "statistics",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            Statistics.serializer(),
            Statistics()
        )
        val ALARM_ITEMS = MultiplatformDataStore(
            "alarm_items",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            AlarmItems.serializer(),
            AlarmItems()
        )
        val ORG_ITEMS = MultiplatformDataStore(
            "org_items",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            OrgItems.serializer(),
            OrgItems()
        )
        val INCODE = MultiplatformDataStore(
            "incode",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            Incode.serializer(),
            Incode()
        )
        val SELF = MultiplatformDataStore(
            "self",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            Employee.serializer(),
            Employee()
        )
        val UPCOMING_DUTIES = MultiplatformDataStore(
            "upcoming_duties",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            UpcomingDutyItems.serializer(),
            UpcomingDutyItems()
        )
        val EMPLOYEES = MultiplatformDataStore(
            "employees",
            null,
            onUpdate = { store, newData -> storageApi.update(store, newData) },
            EmployeeItems.serializer(),
            EmployeeItems()
        )
    }

    private val storageApi = getPlatformStorageApi()

    suspend fun initialize() {
        storageApi.initialize(
            listOf(
                Stores.USER_PREFERENCES,
                Stores.STATISTICS,
                Stores.ALARM_ITEMS,
                Stores.ORG_ITEMS,
                Stores.INCODE,
                Stores.SELF,
                Stores.UPCOMING_DUTIES,
                Stores.EMPLOYEES
            )
        )
    }

}