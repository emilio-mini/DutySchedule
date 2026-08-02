package me.emiliomini.dutyschedule.shared.mappings

import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.notifications_channel_alarms_description
import dutyschedule.shared.generated.resources.notifications_channel_alarms_title
import dutyschedule.shared.generated.resources.notifications_channel_info_description
import dutyschedule.shared.generated.resources.notifications_channel_info_title
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationChannel
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority


object NotificationChannelMapping {
    val ALARMS = MultiplatformNotificationChannel(
        "alarms", Res.string.notifications_channel_alarms_title,
        Res.string.notifications_channel_alarms_description,
        MultiplatformNotificationPriority.URGENT
    )
    val PERMANENT_INFO = MultiplatformNotificationChannel(
        "info", Res.string.notifications_channel_info_title,
        Res.string.notifications_channel_info_description,
        MultiplatformNotificationPriority.LOW
    )
}
