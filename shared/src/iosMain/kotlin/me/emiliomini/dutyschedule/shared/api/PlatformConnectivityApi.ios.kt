package me.emiliomini.dutyschedule.shared.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

class IosConnectivityApi : PlatformConnectivityApi {
    override val isConnected: StateFlow<Boolean> = flowOf(true).stateIn(
        scope = applicationScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
}

actual fun initializePlatformConnectivityApi(): PlatformConnectivityApi {
    return IosConnectivityApi()
}