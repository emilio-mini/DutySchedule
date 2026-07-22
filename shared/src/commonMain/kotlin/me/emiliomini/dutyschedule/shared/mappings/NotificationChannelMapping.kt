package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationChannel
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority


object NotificationChannelMapping {
    val ALARMS = MultiplatformNotificationChannel(
        "alarms", "Duty Alarms", "Channel for duty alarms",
        MultiplatformNotificationPriority.URGENT
    )
    val PERMANENT_INFO = MultiplatformNotificationChannel(
        "info", "Duty Infos", "Channel for duty infos", MultiplatformNotificationPriority.LOW
    )
}
