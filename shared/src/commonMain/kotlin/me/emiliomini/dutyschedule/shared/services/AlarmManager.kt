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
import org.jetbrains.compose.resources.getString
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object AlarmManager {
    @OptIn(ExperimentalTime::class)
    suspend fun updateAlarm(alarm: Alarm, enabled: Boolean, snackbarHostState: SnackbarHostState) {
            if (enabled) {
                setAlarm(alarm.code, Instant.fromEpochMilliseconds(alarm.timestamp), snackbarHostState = snackbarHostState)
            } else {
                getPlatformAlarmApi().cancelAlarm(alarm.code)
            }
    }
    @OptIn(ExperimentalTime::class)
    suspend fun setAlarm(id: Int, time: Instant, zone: TimeZone = TimeZone.currentSystemDefault(), snackbarHostState: SnackbarHostState){
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

            setAlarm(id, time, zone)
        }
    }
}