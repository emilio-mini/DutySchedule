package me.emiliomini.dutyschedule.shared.api

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

@OptIn(ExperimentalForeignApi::class)
class IosConnectivityApi : PlatformConnectivityApi {
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Kept alive for the process lifetime - NWPathMonitor's callback-based C API
    // (nw_path_monitor_t) is the only variant Kotlin/Native can bind to; the ergonomic
    // Swift-only `NWPathMonitor` class has no Objective-C/C header to interop against.
    private val monitor = nw_path_monitor_create()

    init {
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(monitor) { path ->
            _isConnected.value = path != null && nw_path_get_status(path) == nw_path_status_satisfied
        }
        nw_path_monitor_start(monitor)
    }
}

actual fun initializePlatformConnectivityApi(): PlatformConnectivityApi {
    return IosConnectivityApi()
}
