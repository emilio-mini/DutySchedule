package me.emiliomini.dutyschedule.shared.services

import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.error_permissions_missing_alarm
import dutyschedule.shared.generated.resources.error_permissions_missing_alarm_and_notification
import dutyschedule.shared.generated.resources.error_permissions_missing_notification
import kotlinx.datetime.TimeZone
import me.emiliomini.dutyschedule.shared.api.getPlatformAlarmApi
import me.emiliomini.dutyschedule.shared.api.getPlatformNotificationApi
import me.emiliomini.dutyschedule.shared.api.getPlatformTaskSchedulerApi
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.datastores.Alarm
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.toEpochMilliseconds
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object AlarmService {
    @OptIn(ExperimentalTime::class)
    suspend fun updateAlarm(alarm: Alarm, enabled: Boolean, onError: suspend (String) -> Unit) {
            if (enabled) {
                setAlarm(alarm.guid, Instant.fromEpochMilliseconds(alarm.timestamp), onError = onError, edited = true)
            } else {
                getPlatformAlarmApi().cancelAlarm(alarm.guid)
                StorageService.ALARM_ITEMS.update {
                    val index = it.alarms.indexOfFirst { existing -> existing.guid == alarm.guid }
                    if (index == -1) {
                        return@update it
                    }

                    val newDuties = it.alarms.toMutableList()
                    newDuties[index] = it.alarms[index].copy(edited = true)
                    AlarmItems(newDuties)
                }
            }
    }

    /**
     * Reports the missing permissions through [onError] and returns whether an alarm may be set.
     * Only requests them when [onError] is given; requesting opens a settings screen, which must
     * not happen while running headless in [me.emiliomini.dutyschedule.shared.api.TaskRunnerService]
     */
    private suspend fun hasAlarmPermissions(onError: (suspend (String) -> Unit)?): Boolean {
        val mayRequest = onError != null
        val alarmApi = getPlatformAlarmApi()
        val notificationApi = getPlatformNotificationApi()

        val alarmPermission = when {
            alarmApi.isPermissionGranted() -> true
            mayRequest -> alarmApi.requestPermission()
            else -> false
        }
        val notificationPermission = when {
            notificationApi.isPermissionGranted() -> true
            mayRequest -> notificationApi.requestPermission()
            else -> false
        }

        val errorMessage = when {
            !alarmPermission && !notificationPermission -> Res.string.error_permissions_missing_alarm_and_notification
            !alarmPermission -> Res.string.error_permissions_missing_alarm
            !notificationPermission -> Res.string.error_permissions_missing_notification
            else -> return true
        }

        onError?.invoke(getString(errorMessage))
        return false
    }

    /**
     * Returns whether the alarm is set afterwards; false when a permission is missing or the
     * platform refused to schedule it
     */
    @OptIn(ExperimentalTime::class)
    suspend fun setAlarm(guid: String, time: Instant, zone: TimeZone = TimeZone.currentSystemDefault(), onError: suspend (String) -> Unit, edited: Boolean): Boolean {
        if (!hasAlarmPermissions(onError)) {
            return false
        }

        val alarmApi = getPlatformAlarmApi()
        alarmApi.setAlarm(guid, time, zone, edited)

        return alarmApi.isAlarmSet(guid)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun alarmTimeFor(duty: MinimalDutyDefinition): Instant {
        val alarmOffset = StorageService.USER_PREFERENCES.getOrDefault().alarmOffsetMin

        return duty.begin.toInstant() - alarmOffset.minutes
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setAllAlarms(onError: suspend (String) -> Unit){
        val userPreferences = StorageService.USER_PREFERENCES
        userPreferences.update {
            it.copy(autoSetAlarms = true)
        }

        getPlatformTaskSchedulerApi().scheduleTask(MultiplatformTask.UpdateAlarms)
        val upcomingDuties = StorageService.UPCOMING_DUTIES.get()?.minimalDutyDefinitions
        if (upcomingDuties != null){
            updateAlarms(emptyList(), upcomingDuties, onError)
        }
        NotificationService.sendInfoNotification()
    }

    suspend fun cancelAllUneditedAlarms() {
        StorageService.USER_PREFERENCES.update {
            it.copy(autoSetAlarms = false)
        }
        StorageService.USER_PREFERENCES.get()

        getPlatformTaskSchedulerApi().cancelTask(MultiplatformTask.UpdateAlarms)

        val alarms = StorageService.ALARM_ITEMS
        alarms.get()?.alarms?.forEach {
            if (!it.edited){
                getPlatformAlarmApi().cancelAlarm(it.guid)
            }
        }

        alarms.update {
            it.copy(
                alarms = it.alarms.filter {
                    return@filter it.edited
                }
            )
        }
        NotificationService.sendInfoNotification()
    }

    suspend fun removeAlarm(guid: String) {
        getPlatformAlarmApi().cancelAlarm(guid)
        StorageService.ALARM_ITEMS.update {
            it.copy(
                alarms = it.alarms.filter { it.guid != guid }
            )
        }
    }

    /** Returns whether the duties could be refreshed */
    suspend fun fetchAlarms(): Boolean {
        DutyScheduleService.restoreLogin()
        return DutyScheduleService.loadUpcoming() != null
    }

    /**
     * Permissions are resolved once for the whole batch; asking per duty opens the settings screen
     * once per upcoming duty
     */
    @OptIn(ExperimentalTime::class)
    suspend fun updateAlarms(oldDuties: List<MinimalDutyDefinition>, newDuties: List<MinimalDutyDefinition>, onError: (suspend (String) -> Unit)? = null) {
        val alarms = StorageService.ALARM_ITEMS.get()?.alarms
        val oldDutyGuids = oldDuties.map { it.guid }.toMutableList()
        val prefs = StorageService.USER_PREFERENCES.getOrDefault()
        val alarmOffsetMillis = prefs.alarmOffsetMin * 60_000L
        val now = Clock.System.now().toEpochMilliseconds()

        val pending = if (prefs.autoSetAlarms) {
            newDuties.mapNotNull { duty ->
                if (duty.begin.toEpochMilliseconds() - alarmOffsetMillis < now) {
                    return@mapNotNull null
                }

                oldDutyGuids.remove(duty.guid)

                val alarm = alarms?.firstOrNull { it.guid == duty.guid }
                if (alarm != null && alarm.edited && !alarm.active) {
                    return@mapNotNull null
                }

                PendingAlarm(duty, edited = false)
            }
        } else {
            alarms.orEmpty().mapNotNull { alarm ->
                if (!alarm.active) {
                    return@mapNotNull null
                }

                val duty = newDuties.firstOrNull { it.guid == alarm.guid } ?: return@mapNotNull null
                if (duty.begin.toEpochMilliseconds() - alarmOffsetMillis < now) {
                    return@mapNotNull null
                }

                PendingAlarm(duty, alarm.edited)
            }
        }

        if (pending.isNotEmpty() && hasAlarmPermissions(onError)) {
            pending.forEach {
                getPlatformAlarmApi().setAlarm(it.duty.guid, alarmTimeFor(it.duty), edited = it.edited)
            }
        }

        if (prefs.autoSetAlarms) {
            oldDutyGuids.forEach { removeAlarm(it) }
        }

        NotificationService.sendInfoNotification()
    }

    private data class PendingAlarm(val duty: MinimalDutyDefinition, val edited: Boolean)
}