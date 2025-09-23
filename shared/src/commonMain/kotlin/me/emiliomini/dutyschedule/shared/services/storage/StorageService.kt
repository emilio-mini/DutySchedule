package me.emiliomini.dutyschedule.shared.services.storage

import me.emiliomini.dutyschedule.shared.api.getPlatformStorageApi
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.datastores.ClientCookies
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.EmployeeItems
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.datastores.Statistics
import me.emiliomini.dutyschedule.shared.datastores.UpcomingDutyItems
import me.emiliomini.dutyschedule.shared.datastores.UserPreferences

object StorageService {
    val USER_PREFERENCES = MultiplatformDataStore(
        "user_preferences",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        UserPreferences.serializer(),
        UserPreferences()
    )
    val STATISTICS = MultiplatformDataStore(
        "statistics",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        Statistics.serializer(),
        Statistics()
    )
    val ALARM_ITEMS = MultiplatformDataStore(
        "alarm_items",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        AlarmItems.serializer(),
        AlarmItems()
    )
    val ORG_ITEMS = MultiplatformDataStore(
        "org_items",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        OrgItems.serializer(),
        OrgItems()
    )
    val INCODE = MultiplatformDataStore(
        "incode",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        Incode.serializer(),
        Incode()
    )
    val SELF = MultiplatformDataStore(
        "self",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        Employee.serializer(),
        Employee()
    )
    val UPCOMING_DUTIES = MultiplatformDataStore(
        "upcoming_duties",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        UpcomingDutyItems.serializer(),
        UpcomingDutyItems()
    )
    val EMPLOYEES = MultiplatformDataStore(
        "employees",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        EmployeeItems.serializer(),
        EmployeeItems()
    )
    val COOKIES = MultiplatformDataStore(
        "cookies",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        ClientCookies.serializer(),
        ClientCookies()
    )

    private val storageApi = getPlatformStorageApi()

    suspend fun initialize() {
        storageApi.initialize(
            listOf(
                USER_PREFERENCES,
                STATISTICS,
                ALARM_ITEMS,
                ORG_ITEMS,
                INCODE,
                SELF,
                UPCOMING_DUTIES,
                EMPLOYEES,
                COOKIES
            )
        )
    }

    suspend fun clear() {
        USER_PREFERENCES.clear()
        STATISTICS.clear()
        ALARM_ITEMS.clear()
        ORG_ITEMS.clear()
        INCODE.clear()
        SELF.clear()
        UPCOMING_DUTIES.clear()
        EMPLOYEES.clear()
        COOKIES.clear()
    }

}