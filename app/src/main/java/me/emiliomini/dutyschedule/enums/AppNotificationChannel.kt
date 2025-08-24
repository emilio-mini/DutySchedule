package me.emiliomini.dutyschedule.enums

import android.app.NotificationManager

enum class AppNotificationChannel(val id: String, val channelName: String, val description: String, val importance: Int) {
    UPDATES("updates", "App Updates", "Channel for app updates", NotificationManager.IMPORTANCE_DEFAULT),
    ALARMS("alarms", "Duty Alarms", "Channel for duty alarms", NotificationManager.IMPORTANCE_MAX)
}