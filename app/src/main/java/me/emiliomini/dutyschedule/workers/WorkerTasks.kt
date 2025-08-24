package me.emiliomini.dutyschedule.workers

import android.app.PendingIntent
import android.content.Context
import androidx.work.ListenableWorker
import me.emiliomini.dutyschedule.BuildConfig
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.services.api.NetworkService
import me.emiliomini.dutyschedule.enums.AppNotificationChannel
import me.emiliomini.dutyschedule.services.notifications.NotificationService
import me.emiliomini.dutyschedule.util.IntentUtil

object WorkerTasks {

    suspend fun checkForUpdate(context: Context): ListenableWorker.Result {
        val latest = NetworkService.getLatestVersion(true).getOrNull()

        if (latest == null || latest.tag == BuildConfig.VERSION_NAME) {
            return ListenableWorker.Result.success()
        }

        NotificationService.send(
            context,
            NotificationService.simpleNotification(
                context,
                AppNotificationChannel.UPDATES,
                context.getString(R.string.notification_update_title),
                context.getString(
                    R.string.notification_update_content,
                    BuildConfig.VERSION_NAME,
                    latest.tag
                ),
                pendingIntent = PendingIntent.getActivity(
                    context, 0, IntentUtil.getAppIntent(context),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        )
        return ListenableWorker.Result.success()
    }
}