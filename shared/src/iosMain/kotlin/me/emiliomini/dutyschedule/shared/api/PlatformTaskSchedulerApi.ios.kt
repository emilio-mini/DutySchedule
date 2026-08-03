package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask

/**
 * Background work is not wired up on iOS yet. The only scheduled task refreshes duty alarms, which
 * iOS does not have either, so this accepts and drops the request rather than throwing - the app
 * schedules it during startup and a failure there takes the whole launch down.
 *
 * Implementing it means BGTaskScheduler, a matching BGTaskSchedulerPermittedIdentifiers entry and
 * the processing background mode in Info.plist.
 * https://developer.apple.com/documentation/backgroundtasks/bgtaskscheduler
 */
class IosTaskSchedulerApi : PlatformTaskSchedulerApi {
    private val logger = getPlatformLogger("IosTaskSchedulerApi")

    override fun scheduleTask(task: MultiplatformTask) {
        logger.d("Ignoring $task - background tasks are not supported on iOS yet")
    }

    override fun cancelTask(task: MultiplatformTask) {
        logger.d("Ignoring cancel of $task - background tasks are not supported on iOS yet")
    }
}

actual fun initializePlatformTaskSchedulerApi(): PlatformTaskSchedulerApi {
    return IosTaskSchedulerApi()
}