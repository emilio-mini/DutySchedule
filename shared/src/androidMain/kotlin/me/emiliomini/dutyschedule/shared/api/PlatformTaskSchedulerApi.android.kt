package me.emiliomini.dutyschedule.shared.api

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AndroidTaskSchedulerApi() : PlatformTaskSchedulerApi {


    override fun scheduleTask(task: MultiplatformTask) {


        val notificationApi = getPlatformNotificationApi() as AndroidNotificationApi
        val notification = MultiplatformNotification(38, NotificationChannelMapping.ALARMS,
            MultiplatformNotificationPriority.NORMAL, "Test Starter", "Test Starter ${Calendar.getInstance().apply { timeInMillis = WorkManager.getInstance(APPLICATION_CONTEXT).getWorkInfosForUniqueWork(task.name).get().firstOrNull()?.nextScheduleTimeMillis ?: 0 }.toInstant()}")
        notificationApi.send(notification)


        val data = Data.Builder()
            .putString("task", task.name)
            .build()

        val networkConstraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<TaskRunnerService>(1, TimeUnit.HOURS, 3, TimeUnit.HOURS)
            .setInputData(data)
            .setConstraints(networkConstraint)
            .setNextScheduleTimeOverride(sevenPmInMillis())
            .build()

            WorkManager.getInstance(APPLICATION_CONTEXT).enqueueUniquePeriodicWork(
            task.name,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancelTask(task: MultiplatformTask) {
        WorkManager.getInstance(APPLICATION_CONTEXT).cancelUniqueWork(task.name)
    }

    private fun sevenPmInMillis(): Long {
        val now = Calendar.getInstance()

        val sevenPm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If it's already past 7 PM today, schedule for tomorrow
        if (sevenPm.before(now)) {
            sevenPm.add(Calendar.DAY_OF_YEAR, 1)
        }

        return sevenPm.timeInMillis //- now.timeInMillis
    }
}

actual fun initializePlatformTaskSchedulerApi(): PlatformTaskSchedulerApi {
    return AndroidTaskSchedulerApi()
}


