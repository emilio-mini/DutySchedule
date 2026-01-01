package me.emiliomini.dutyschedule.shared.api

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.services.AlarmService

class TaskRunnerService(ctx: Context, params: WorkerParameters): CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val task = inputData.getString("task")?.let { MultiplatformTask.valueOf(it) }

        when(task){
            MultiplatformTask.UpdateAlarms -> {
                AlarmService.fetchAlarms()
                return Result.success()
            }
            null -> return Result.failure()
        }
    }
}