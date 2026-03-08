package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask

interface PlatformTaskSchedulerApi {
    fun scheduleTask(task: MultiplatformTask)
    fun cancelTask(task: MultiplatformTask)
}

expect fun initializePlatformTaskSchedulerApi(): PlatformTaskSchedulerApi


private var api: PlatformTaskSchedulerApi? = null

fun getPlatformTaskSchedulerApi(): PlatformTaskSchedulerApi = if (api == null) {
    api = initializePlatformTaskSchedulerApi()
    api!!
} else {
    api!!
}