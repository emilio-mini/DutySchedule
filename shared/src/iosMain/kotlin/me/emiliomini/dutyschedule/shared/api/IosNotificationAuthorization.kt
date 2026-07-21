package me.emiliomini.dutyschedule.shared.api

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS has no synchronous "is authorized" query (UNUserNotificationCenter only exposes async
 * completion-handler APIs), but [PlatformAlarmApi] and [PlatformNotificationApi] both declare
 * synchronous `isPermissionGranted()`/`requestPermission()` methods to stay source-compatible
 * with Android. This caches the last known status so both APIs (which share the same underlying
 * OS permission) can answer synchronously, refreshing it whenever we get a real answer.
 *
 * Mirrors Android's own imperfection here: like `AndroidAlarmApi.requestPermission()`, the
 * returned value reflects the status *before* the just-fired request is answered by the user.
 */
internal object IosNotificationAuthorization {
    private val logger = getPlatformLogger("IosNotificationAuthorization")
    private var cachedAuthorized = false

    init {
        refresh()
    }

    fun isGranted(): Boolean = cachedAuthorized

    fun request(): Boolean {
        val previous = cachedAuthorized
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            cachedAuthorized = granted
            if (error != null) {
                logger.warn("Notification authorization request failed: ${error.localizedDescription}")
            }
        }
        return previous
    }

    fun refresh() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            cachedAuthorized = status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional
        }
    }
}
