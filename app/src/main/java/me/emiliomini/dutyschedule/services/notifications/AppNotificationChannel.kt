package me.emiliomini.dutyschedule.services.notifications

import android.app.NotificationManager

enum class AppNotificationChannel(val id: String, val channelName: String, val description: String, val importance: Int) {
    UPDATES("updates", "App Updates", "Channel for app updates", NotificationManager.IMPORTANCE_DEFAULT)
}