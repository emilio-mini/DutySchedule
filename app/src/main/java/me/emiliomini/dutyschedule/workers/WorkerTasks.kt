package me.emiliomini.dutyschedule.workers

import android.app.PendingIntent
import android.content.Context
import androidx.work.ListenableWorker
import me.emiliomini.dutyschedule.BuildConfig
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.services.network.NetworkService
import me.emiliomini.dutyschedule.enums.AppNotificationChannel
import me.emiliomini.dutyschedule.services.notifications.NotificationService
import me.emiliomini.dutyschedule.util.IntentUtil

object WorkerTasks {

    suspend fun checkForUpdate(context: Context): ListenableWorker.Result {
        return ListenableWorker.Result.success()
    }
}