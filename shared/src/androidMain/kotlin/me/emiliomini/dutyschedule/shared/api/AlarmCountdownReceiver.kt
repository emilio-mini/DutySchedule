package me.emiliomini.dutyschedule.shared.api

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.notifications_countdown_action_dismiss
import dutyschedule.shared.generated.resources.notifications_countdown_content
import dutyschedule.shared.generated.resources.notifications_countdown_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.emiliomini.dutyschedule.shared.R
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import me.emiliomini.dutyschedule.shared.mappings.NotificationIds
import me.emiliomini.dutyschedule.shared.services.AlarmService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.format
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Owns the silent countdown notification shown in the hours before an alarm rings. Handles both the
 * scheduled trigger that posts it and the dismiss button, which cancels the alarm itself
 */
@OptIn(ExperimentalTime::class)
class AlarmCountdownReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val guid = intent?.getStringExtra(EXTRA_GUID) ?: return
        APPLICATION_CONTEXT = context.applicationContext

        when (intent.action) {
            ACTION_SHOW -> show(guid, intent.getLongExtra(EXTRA_ALARM_AT, 0L))
            ACTION_DISMISS -> dismiss(guid)
        }
    }

    private fun show(guid: String, alarmAt: Long) {
        if (alarmAt <= System.currentTimeMillis()) {
            return
        }

        show(APPLICATION_CONTEXT, guid, alarmAt)
    }

    private fun dismiss(guid: String) {
        val pending = goAsync()
        scope.launch {
            try {
                StorageService.initialize()
                AlarmService.removeAlarm(guid)
            } finally {
                hide(APPLICATION_CONTEXT, guid)
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_SHOW = "me.emiliomini.dutyschedule.alarm.COUNTDOWN_SHOW"
        const val ACTION_DISMISS = "me.emiliomini.dutyschedule.alarm.COUNTDOWN_DISMISS"
        const val EXTRA_GUID = "guid"
        const val EXTRA_ALARM_AT = "alarm_at"

        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        /**
         * Posts an ongoing notification whose chronometer counts down to [alarmAt]. The system
         * renders the ticking timer, so nothing here has to keep updating it
         */
        fun show(context: Context, guid: String, alarmAt: Long) {
            val notificationApi = getPlatformNotificationApi() as AndroidNotificationApi
            notificationApi.verifyOrCreateChannel(NotificationChannelMapping.ALARM_COUNTDOWN)

            if (!notificationApi.isPermissionGranted()) {
                return
            }

            val dismissIntent = Intent(context, AlarmCountdownReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_GUID, guid)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                dismissRequestCode(guid),
                dismissIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val ringingAt = Instant.fromEpochMilliseconds(alarmAt).format("HH:mm")
            val title = runBlocking { getString(Res.string.notifications_countdown_title) }
            val content =
                runBlocking { getString(Res.string.notifications_countdown_content, ringingAt) }
            val dismiss =
                runBlocking { getString(Res.string.notifications_countdown_action_dismiss) }

            val notification =
                NotificationCompat.Builder(context, NotificationChannelMapping.ALARM_COUNTDOWN.id)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setSilent(true)
                    .setShowWhen(true)
                    .setWhen(alarmAt)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .addAction(0, dismiss, dismissPendingIntent)
                    .setContentIntent(launchPendingIntent(context))
                    .build()

            NotificationManagerCompat.from(context)
                .notify(NotificationIds.countdown(guid), notification)
        }

        fun hide(context: Context, guid: String) {
            NotificationManagerCompat.from(context).cancel(NotificationIds.countdown(guid))
        }

        fun dismissRequestCode(guid: String) = "countdown-dismiss:$guid".hashCode()

        private fun launchPendingIntent(context: Context): PendingIntent? {
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(context.packageName) ?: return null

            return PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
