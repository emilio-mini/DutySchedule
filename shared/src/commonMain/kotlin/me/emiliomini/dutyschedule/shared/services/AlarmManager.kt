package me.emiliomini.dutyschedule.shared.services

import androidx.compose.material3.SnackbarHostState
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.error_permissions_missing_alarm
import dutyschedule.shared.generated.resources.error_permissions_missing_alarm_and_notification
import dutyschedule.shared.generated.resources.error_permissions_missing_notification
import kotlinx.datetime.TimeZone
import me.emiliomini.dutyschedule.shared.api.getPlatformAlarmApi
import me.emiliomini.dutyschedule.shared.api.getPlatformNotificationApi
import me.emiliomini.dutyschedule.shared.datastores.Alarm
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.toEpochMilliseconds
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object AlarmManager {
    @OptIn(ExperimentalTime::class)
    suspend fun updateAlarm(alarm: Alarm, enabled: Boolean, snackbarHostState: SnackbarHostState) {
            if (enabled) {
                setAlarm(alarm.guid, Instant.fromEpochMilliseconds(alarm.timestamp), snackbarHostState = snackbarHostState, edited = true)
            } else {
                getPlatformAlarmApi().cancelAlarm(alarm.code)
            }
    }
    @OptIn(ExperimentalTime::class)
    suspend fun setAlarm(guid: String, time: Instant, zone: TimeZone = TimeZone.currentSystemDefault(), snackbarHostState: SnackbarHostState, edited: Boolean){
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
                snackbarHostState.showSnackbar(errorMessage)
                return
            }

            setAlarm(guid, time, zone, edited)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun setAllAlarms(snackbarHostState: SnackbarHostState){
        val upcomingDuties = StorageService.UPCOMING_DUTIES.get()
        val alarmOffset =
            StorageService.USER_PREFERENCES.getOrDefault().alarmOffsetMin
        val alarmOffsetMillis = alarmOffset * 60_000L

        val currentAlarms = StorageService.ALARM_ITEMS.get()?.alarms ?: listOf<Alarm>()


        upcomingDuties?.minimalDutyDefinitions?.forEach {
            val timestamp = it.begin.toEpochMilliseconds() - alarmOffsetMillis
            if (currentAlarms.any { alarm -> alarm.guid == it.guid }){
                return@forEach
            }

            // TODO: Don't spam snackbars if permission is missing.
            setAlarm(it.guid, Instant.fromEpochMilliseconds(timestamp), snackbarHostState = snackbarHostState, edited = false)
        }
    }

    suspend fun cancelAllUneditedAlarms() {
        val alarms = StorageService.ALARM_ITEMS
        alarms.get()?.alarms?.forEach {
            if (!it.edited){
                getPlatformAlarmApi().cancelAlarm(it.code)
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
}