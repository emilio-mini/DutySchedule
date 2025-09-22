package me.emiliomini.dutyschedule.shared.mappings

enum class NotificationChannelMapping(val id: String, val channelName: String, val description: String, val importance: NotificationImportance) {
    UPDATES("updates", "App Updates", "Channel for app updates", NotificationImportance.DEFAULT),
    ALARMS("alarms", "Duty Alarms", "Channel for duty alarms", NotificationImportance.HIGH)
}

enum class NotificationImportance {
    LOW,
    DEFAULT,
    HIGH,
    MAX
}
