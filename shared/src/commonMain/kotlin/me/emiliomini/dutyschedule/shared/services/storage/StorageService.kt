package me.emiliomini.dutyschedule.shared.services.storage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.emiliomini.dutyschedule.shared.api.getPlatformStorageApi
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.datastores.ClientCookies
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.EndpointConfig
import me.emiliomini.dutyschedule.shared.datastores.EmployeeItems
import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.datastores.OrgItems
import me.emiliomini.dutyschedule.shared.datastores.PastDutyItems
import me.emiliomini.dutyschedule.shared.datastores.Statistics
import me.emiliomini.dutyschedule.shared.datastores.UpcomingDutyItems
import me.emiliomini.dutyschedule.shared.datastores.UserPreferences
import me.emiliomini.dutyschedule.shared.services.CredentialService

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
    val PAST_DUTIES = MultiplatformDataStore(
        "past_duties",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        PastDutyItems.serializer(),
        PastDutyItems()
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

    val ENDPOINTS = MultiplatformDataStore(
        "endpoints",
        onUpdate = { store, newData -> storageApi.update(store, newData) },
        EndpointConfig.serializer(),
        EndpointConfig()
    )

    val ALL_STORES = listOf(
        USER_PREFERENCES,
        STATISTICS,
        ALARM_ITEMS,
        ORG_ITEMS,
        INCODE,
        SELF,
        UPCOMING_DUTIES,
        PAST_DUTIES,
        EMPLOYEES,
        COOKIES,
        ENDPOINTS
    )

    /**
     * Everything except the configured endpoints. Those describe which server the app talks to, not
     * who is signed in, so logging out must not send the user back to onboarding without them.
     */
    private val CLEARED_ON_LOGOUT = ALL_STORES - ENDPOINTS

    private val storageApi = getPlatformStorageApi()
    private val initMutex = Mutex()
    private var initialized = false

    /**
     * Builds the platform stores once per process; the app entry point, a restart and the
     * background worker all reach this
     */
    suspend fun initialize() {
        initMutex.withLock {
            if (initialized) {
                return
            }

            storageApi.initialize(ALL_STORES)
            ALL_STORES.forEach {
                it.ensureLoaded()
            }
            initialized = true
        }

        CredentialService.migrateLegacyPlaintextPassword()
    }

    suspend fun clear() {
        CLEARED_ON_LOGOUT.forEach {
            it.clear()
        }
    }

}