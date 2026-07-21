@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import me.emiliomini.dutyschedule.shared.mappings.NotificationIds
import me.emiliomini.dutyschedule.shared.services.AlarmService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Must exactly match an entry in the `BGTaskSchedulerPermittedIdentifiers` array in Info.plist,
 * or registration/submission silently fails.
 */
const val IOS_BACKGROUND_REFRESH_TASK_IDENTIFIER = "me.emiliomini.dutyscheduleIOS.refresh"

private val taskScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

class IosTaskSchedulerApi : PlatformTaskSchedulerApi {
    private val logger = getPlatformLogger("IosTaskSchedulerApi")

    override fun scheduleTask(task: MultiplatformTask) {
        when (task) {
            MultiplatformTask.UpdateAlarms -> submitRefreshRequest()
        }
    }

    override fun cancelTask(task: MultiplatformTask) {
        when (task) {
            MultiplatformTask.UpdateAlarms ->
                BGTaskScheduler.shared.cancelTaskRequestWithIdentifier(IOS_BACKGROUND_REFRESH_TASK_IDENTIFIER)
        }
    }

    private fun submitRefreshRequest() {
        val request = BGAppRefreshTaskRequest(identifier = IOS_BACKGROUND_REFRESH_TASK_IDENTIFIER)
        request.earliestBeginDate = nextSevenPmAsNSDate()

        val success = BGTaskScheduler.shared.submitTaskRequest(request, null)
        if (!success) {
            // Most commonly: identifier not declared in Info.plist, or running in the simulator
            // without a scheme launch-argument override - not fatal, just means no background run.
            logger.warn("Failed to submit background refresh request for $IOS_BACKGROUND_REFRESH_TASK_IDENTIFIER")
        }
    }

    private fun nextSevenPmAsNSDate(): NSDate {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(zone).date

        var target = LocalDateTime(today, LocalTime(hour = 19, minute = 0)).toInstant(zone)
        if (target <= now) {
            target = LocalDateTime(today.plus(1, DateTimeUnit.DAY), LocalTime(hour = 19, minute = 0)).toInstant(zone)
        }

        val epochSeconds = target.epochSeconds.toDouble() + (target.nanosecondsOfSecond / 1_000_000_000.0)
        return NSDate.dateWithTimeIntervalSince1970(epochSeconds)
    }
}

actual fun initializePlatformTaskSchedulerApi(): PlatformTaskSchedulerApi = IosTaskSchedulerApi()

/**
 * Registers the background-refresh launch handler with [BGTaskScheduler]. Per Apple's docs this
 * must run before the app finishes launching. Not public: Swift reaches this indirectly through
 * [IosPlatformBridge.registerBackgroundTasks], which has a stable, predictable Swift export name.
 */
internal fun registerIosBackgroundTasks() {
    BGTaskScheduler.shared.registerForTaskWithIdentifier(
        IOS_BACKGROUND_REFRESH_TASK_IDENTIFIER,
        usingQueue = null
    ) { task ->
        if (task != null) {
            handleRefreshTask(task)
        }
    }
}

private fun handleRefreshTask(task: BGTask) {
    val job = taskScope.launch {
        try {
            StorageService.initialize()
            AlarmService.fetchAlarms()

            val notification = MultiplatformNotification(
                NotificationIds.DUTY_UPDATE,
                NotificationChannelMapping.ALARMS,
                MultiplatformNotificationPriority.NORMAL,
                "Duty Update",
                "Updated duties at ${Clock.System.now()}"
            )
            getPlatformNotificationApi().send(notification)

            task.setTaskCompletedWithSuccess(true)
        } catch (e: Throwable) {
            task.setTaskCompletedWithSuccess(false)
        } finally {
            // BGAppRefreshTask requests are one-shot: resubmit so there's another run tomorrow.
            getPlatformTaskSchedulerApi().scheduleTask(MultiplatformTask.UpdateAlarms)
        }
    }

    task.expirationHandler = {
        job.cancel()
    }
}
