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
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.toEpochMilliseconds
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object AlarmService {
    @OptIn(ExperimentalTime::class)
    suspend fun updateAlarm(alarm: Alarm, enabled: Boolean, onError: suspend (String) -> Unit) {
            if (enabled) {
                setAlarm(alarm.guid, Instant.fromEpochMilliseconds(alarm.timestamp), onError = onError, edited = true)
            } else {
                getPlatformAlarmApi().cancelAlarm(alarm.guid)
            }
    }
    @OptIn(ExperimentalTime::class)
    suspend fun setAlarm(guid: String, time: Instant, zone: TimeZone = TimeZone.currentSystemDefault(), onError: suspend (String) -> Unit, edited: Boolean){
        with(getPlatformAlarmApi()){

            val alarmPermission = requestPermission()
            val notificationPermission = getPlatformNotificationApi().requestPermission()

            val errorMessage = when {
                !alarmPermission && !notificationPermission -> getString(Res.string.error_permissions_missing_alarm_and_notification)
                !alarmPermission -> getString(Res.string.error_permissions_missing_alarm)
                !notificationPermission -> getString(Res.string.error_permissions_missing_notification)
                else -> null
            }

            if (errorMessage != null) {
                onError(errorMessage)
                return
            }

            setAlarm(guid, time, zone, edited)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setAlarm(duty: MinimalDutyDefinition, onError: suspend (String) -> Unit){
        val alarmOffset =
            StorageService.USER_PREFERENCES.getOrDefault().alarmOffsetMin
        val alarmOffsetMillis = alarmOffset * 60_000L

        val timestamp = duty.begin.toEpochMilliseconds() - alarmOffsetMillis

        setAlarm(duty.guid, Instant.fromEpochMilliseconds(timestamp), onError = onError, edited = false)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setAllAlarms(onError: suspend (String) -> Unit){
        getPlatformTaskSchedulerApi().scheduleTask(MultiplatformTask.UpdateAlarms)
        val upcomingDuties = StorageService.UPCOMING_DUTIES.get()

        val currentAlarms = StorageService.ALARM_ITEMS.get()?.alarms ?: listOf<Alarm>()


        upcomingDuties?.minimalDutyDefinitions?.forEach {
            if (currentAlarms.any { alarm -> alarm.guid == it.guid }){
                return@forEach
            }

            // TODO: Don't spam snackbars if permission is missing.
            setAlarm(it, onError)
        }
    }

    suspend fun cancelAllUneditedAlarms() {
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
    }

    suspend fun removeAlarm(guid: String) {
        getPlatformAlarmApi().cancelAlarm(guid)
        StorageService.ALARM_ITEMS.update {
            it.copy(
                alarms = it.alarms.filter { it.guid != guid }
            )
        }
    }

    suspend fun fetchAlarms() {
        val oldDuties = StorageService.UPCOMING_DUTIES.get()?.minimalDutyDefinitions?.map { it.guid }

        DutyScheduleService.loadUpcoming()

        val updatedDuties = StorageService.UPCOMING_DUTIES.get()?.minimalDutyDefinitions



        updatedDuties?.forEach {
            oldDuties?.minus(it.guid)

            setAlarm(it, onError = { })
        }

        oldDuties?.forEach { removeAlarm(it) }
    }
}