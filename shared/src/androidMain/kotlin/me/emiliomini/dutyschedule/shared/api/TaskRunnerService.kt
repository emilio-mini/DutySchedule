package me.emiliomini.dutyschedule.shared.api

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import me.emiliomini.dutyschedule.shared.services.AlarmService
import java.util.Calendar

class TaskRunnerService(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val task = inputData.getString("task")?.let { MultiplatformTask.valueOf(it) }

        when(task){
            MultiplatformTask.UpdateAlarms -> {
                APPLICATION_CONTEXT = this.applicationContext;
                AlarmService.fetchAlarms()

                val notification = MultiplatformNotification(37, NotificationChannelMapping.ALARMS,
                    MultiplatformNotificationPriority.NORMAL, "Duty Update", "Updated Duties at ${Calendar.getInstance().time}") // TODO Internationalize
                val notificationApi = getPlatformNotificationApi() as AndroidNotificationApi
                notificationApi.send(notification)

                getPlatformTaskSchedulerApi().scheduleTask(MultiplatformTask.UpdateAlarms)

                return Result.success()
            }
            null -> return Result.failure()
        }
    }
}