@file:OptIn(ExperimentalForeignApi::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

/**
 * Reports real reachability. This used to be hardcoded to true, which left every offline path in
 * the shared code dead: requests were attempted instead of short circuited, and the reconnect that
 * restores the login never fired.
 */
class IosConnectivityApi : PlatformConnectivityApi {
    private val connectedState = MutableStateFlow(false)

    override val isConnected: StateFlow<Boolean> = connectedState.asStateFlow()

    private val monitor = nw_path_monitor_create()

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            connectedState.value = nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }
}

actual fun initializePlatformConnectivityApi(): PlatformConnectivityApi {
    return IosConnectivityApi()
}