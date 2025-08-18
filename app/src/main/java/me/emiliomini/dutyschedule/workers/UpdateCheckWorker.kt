package me.emiliomini.dutyschedule.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpdateCheckWorker(
    val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val WORK_NAME = "update_check_task"
    }

    override suspend fun doWork(): Result {
        return try {
            return WorkerTasks.checkForUpdate(appContext)
        } catch (e: Exception) {
            Result.retry()
        }
    }


}