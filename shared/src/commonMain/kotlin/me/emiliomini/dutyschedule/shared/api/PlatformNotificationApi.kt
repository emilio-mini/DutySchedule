package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification

interface PlatformNotificationApi {
    fun send(notification: MultiplatformNotification)
    fun dismiss(notification: MultiplatformNotification)
}

expect fun initializePlatformNotificationApi(): PlatformNotificationApi

private var api: PlatformNotificationApi? = null

fun getPlatformNotificationApi(): PlatformNotificationApi = if (api == null) {
    api = initializePlatformNotificationApi()
    api!!
} else {
    api!!
}