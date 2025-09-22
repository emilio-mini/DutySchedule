package me.emiliomini.dutyschedule.workers

import android.content.Context
import androidx.work.ListenableWorker

object WorkerTasks {

    suspend fun checkForUpdate(context: Context): ListenableWorker.Result {
        return ListenableWorker.Result.success()
    }
}