package me.emiliomini.dutyschedule.services.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import me.emiliomini.dutyschedule.R
import java.util.concurrent.atomic.AtomicInteger

object NotificationService {
    private val notificationIdCounter = AtomicInteger(0)

    fun initialize(context: Context) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        AppNotificationChannel.entries.forEach {
            val channel = NotificationChannel(it.id, it.channelName, it.importance).apply {
                description = it.description
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun send(context: Context, notification: Notification, id: Int? = null): Int? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val notificationId = id ?: notificationIdCounter.incrementAndGet()
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }
        return notificationId
    }

    fun simpleNotification(
        context: Context,
        channel: AppNotificationChannel,
        title: String,
        text: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        actions: List<NotificationCompat.Action> = emptyList()
    ): Notification {
        val builder = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)

        for (action in actions) {
            builder.addAction(action)
        }

        return builder.build()
    }
}