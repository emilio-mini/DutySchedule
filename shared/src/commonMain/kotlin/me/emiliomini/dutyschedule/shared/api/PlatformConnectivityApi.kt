package me.emiliomini.dutyschedule.shared.api

import kotlinx.coroutines.flow.StateFlow

interface PlatformConnectivityApi {
    val isConnected: StateFlow<Boolean>
}

expect fun initializePlatformConnectivityApi(): PlatformConnectivityApi

private var api: PlatformConnectivityApi? = null

fun getPlatformConnectivityApi(): PlatformConnectivityApi = if (api == null) {
    api = initializePlatformConnectivityApi()
    api!!
} else {
    api!!
}
