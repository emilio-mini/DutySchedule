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
                    val index = it.alarms.indexOfFirst { it.guid == alarm.guid }
                    val oldAlarm = it.alarms[index]
                    val newDuties = it.alarms.toMutableList()
                    newDuties[index] = oldAlarm.copy(edited = true)
                    AlarmItems(newDuties)
                }
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

    suspend fun fetchAlarms() {
        DutyScheduleService.restoreLogin()
        DutyScheduleService.loadUpcoming()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun updateAlarms(oldDuties: List<MinimalDutyDefinition>, newDuties: List<MinimalDutyDefinition>, onError: (suspend (String) -> Unit)? = null) {
        val alarms = StorageService.ALARM_ITEMS.get()?.alarms
        val oldDutyGuids = oldDuties.map { it.guid }.toMutableList()
        val prefs = StorageService.USER_PREFERENCES.getOrDefault()
        val alarmOffsetMillis = prefs.alarmOffsetMin * 60_000L
        if (prefs.autoSetAlarms){
            newDuties.forEach {
                val alarm = alarms?.firstOrNull { alarm -> alarm.guid == it.guid}
                if (it.begin.toEpochMilliseconds() - alarmOffsetMillis < Clock.System.now().toEpochMilliseconds())
                    return@forEach

                oldDutyGuids.remove(it.guid)

                if (alarm != null && alarm.edited && !alarm.active)
                    return@forEach
                
                setAlarm(it, onError ?: { })
            }

            oldDutyGuids.forEach { removeAlarm(it) }
        } else {
            alarms?.forEach { oldAlarm ->
                val new = newDuties.firstOrNull{it.guid == oldAlarm.guid} ?: return@forEach

                if (new.begin.toInstant() < Clock.System.now() || (oldAlarm.edited && !oldAlarm.active))
                    return@forEach

                setAlarm(new, onError ?: { })
            }
        }
        NotificationService.sendInfoNotification()
    }
}