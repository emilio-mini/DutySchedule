package me.emiliomini.dutyschedule.shared.api

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import me.emiliomini.dutyschedule.shared.R
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationAction
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationChannel
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority
import me.emiliomini.dutyschedule.shared.api.notifications.NotificationActionReceiver
import me.emiliomini.dutyschedule.shared.api.notifications.NotificationActionRegistry
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import kotlin.time.ExperimentalTime


class AndroidNotificationApi : PlatformNotificationApi {
    private var manager: NotificationManager? = null
    private val logger = getPlatformLogger("AndroidAlarmApi")
    override fun requestPermission(): Boolean {
//        if (!isPermissionGranted()){
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                ActivityCompat.requestPermissions(
//                    null,
//                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                    0
//                )
//            }
//            return isPermissionGranted()
//        } else return true
        return true // TODO: Find a way to request the POST_NOTIFICATIONS permission
    }

    override fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ContextCompat.checkSelfPermission(
            APPLICATION_CONTEXT, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED else true
    }

    @OptIn(ExperimentalTime::class)
    override fun send(notification: MultiplatformNotification) {
        logger.d("Sending notification: $notification")
        verifyOrCreateChannel(notification.channel)

        val notificationBuilder =
            NotificationCompat.Builder(APPLICATION_CONTEXT, notification.channel.id)
                .setSmallIcon(R.drawable.ic_notification).setContentTitle(notification.title)
                .setContentText(notification.content).setPriority(notification.priority.android())
                .setAutoCancel(true)

        if (notification.channel.id == NotificationChannelMapping.PERMANENT_INFO.id) {
            notificationBuilder
                .setOngoing(true)
                .setAutoCancel(false)

            val nextAlarm = getPlatformAlarmApi().getNextAlarm()?.toEpochMilliseconds()

            if (nextAlarm != null) {
                notificationBuilder
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(nextAlarm)
            }

            val launchIntent = APPLICATION_CONTEXT.packageManager
                .getLaunchIntentForPackage(APPLICATION_CONTEXT.packageName)
            if (launchIntent != null) {
                val contentPending = PendingIntent.getActivity(
                    APPLICATION_CONTEXT,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.setContentIntent(contentPending)
            }
        }

        if (notification.leftAction != null) {
            val code = NotificationActionRegistry.register(notification.leftAction)

            val actionIntent =
                Intent(APPLICATION_CONTEXT, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_PERFORM
                    putExtra(NotificationActionReceiver.EXTRA_REQUEST_CODE, code)
                }
            val pending = PendingIntent.getBroadcast(
                APPLICATION_CONTEXT,
                code,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(0, notification.leftAction.title, pending)
        }

        if (notification.rightAction != null) {
            val code = NotificationActionRegistry.register(notification.rightAction)

            val actionIntent =
                Intent(APPLICATION_CONTEXT, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_PERFORM
                    putExtra(NotificationActionReceiver.EXTRA_REQUEST_CODE, code)
                }
            val pending = PendingIntent.getBroadcast(
                APPLICATION_CONTEXT,
                code,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(0, notification.rightAction.title, pending)
        }

        if (notification.onDismiss != null) {
            val code =
                NotificationActionRegistry.register(MultiplatformNotificationAction(action = notification.onDismiss))

            val actionIntent =
                Intent(APPLICATION_CONTEXT, NotificationActionReceiver::class.java).apply {
                    action = NotificationActionReceiver.ACTION_PERFORM
                    putExtra(NotificationActionReceiver.EXTRA_REQUEST_CODE, code)
                }
            val pending = PendingIntent.getBroadcast(
                APPLICATION_CONTEXT,
                code,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.setDeleteIntent(pending)
        }

        if (ActivityCompat.checkSelfPermission(
                APPLICATION_CONTEXT,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(APPLICATION_CONTEXT)
                .notify(notification.id, notificationBuilder.build())
        } else {
            logger.d("No POST_NOTIFICATIONS permission") // TODO Internationalize
        }
    }

    override fun dismiss(notification: MultiplatformNotification) {
        NotificationManagerCompat.from(APPLICATION_CONTEXT).cancel(notification.id)
    }

    fun verifyOrCreateChannel(channel: MultiplatformNotificationChannel) {
        val manager = getNotificationManager()
        val exists = manager.getNotificationChannel(channel.id) != null

        if (exists) {
            return
        }

        val androidChannel =
            NotificationChannel(channel.id, channel.title, channel.priority.android()).apply {
                description = channel.description
            }
        getNotificationManager().createNotificationChannel(androidChannel)
    }

    private fun MultiplatformNotificationPriority.android(): Int {
        return when (this) {
            MultiplatformNotificationPriority.LOW -> NotificationManager.IMPORTANCE_LOW
            MultiplatformNotificationPriority.NORMAL -> NotificationManager.IMPORTANCE_DEFAULT
            MultiplatformNotificationPriority.HIGH -> NotificationManager.IMPORTANCE_HIGH
            MultiplatformNotificationPriority.URGENT -> NotificationManager.IMPORTANCE_HIGH
        }
    }

    private fun getNotificationManager(): NotificationManager {
        manager?.let { return it }

        val manager = APPLICATION_CONTEXT.getSystemService(NotificationManager::class.java)
        this.manager = manager
        return manager
    }
}

actual fun initializePlatformNotificationApi(): PlatformNotificationApi {
    return AndroidNotificationApi()
}