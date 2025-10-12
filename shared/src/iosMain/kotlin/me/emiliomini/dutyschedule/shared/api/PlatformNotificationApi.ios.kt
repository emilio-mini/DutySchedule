package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification

class IosNotificationApi : PlatformNotificationApi {
    override fun send(notification: MultiplatformNotification) {
        // TODO: Implement
    }

    override fun dismiss(notification: MultiplatformNotification) {
        // TODO: Implement
    }

}

actual fun initializePlatformNotificationApi(): PlatformNotificationApi {
    return IosNotificationApi()
}
