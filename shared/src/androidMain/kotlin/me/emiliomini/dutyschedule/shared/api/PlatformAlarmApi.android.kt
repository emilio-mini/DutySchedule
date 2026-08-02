@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.datetime.TimeZone
import me.emiliomini.dutyschedule.shared.datastores.Alarm
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.services.AlarmService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AndroidAlarmApi : PlatformAlarmApi {
    private val logger = getPlatformLogger("AndroidAlarmApi")

    override fun requestPermission(): Boolean {
        if (isPermissionGranted()) {
            return true
        }

        openExactAlarmSettings()
        return false
    }

    override fun isPermissionGranted(): Boolean {
        val alarmManager =
            APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        return if (alarmManager != null) {
            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms())
        } else {
            true
        }
    }

    override suspend fun setAlarm(guid: String, time: Instant, zone: TimeZone, edited: Boolean) {
        val triggerAt = time.toEpochMilliseconds()
        if (triggerAt < Clock.System.now().toEpochMilliseconds()) {
            return
        }

        val id = guid.hashCode()
        val alarmManager =
            APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            logger.warn("Cannot schedule exact alarms - skipping alarm $id")
            return
        }

        val info = AlarmManager.AlarmClockInfo(triggerAt, launchPendingIntent())
        alarmManager.setAlarmClock(info, alarmPendingIntent(guid))
        scheduleCountdown(alarmManager, guid, triggerAt)

        StorageService.ALARM_ITEMS.update { alarmItems ->
            val alarms = alarmItems.alarms.toMutableList()
            val alarmIndex = alarms.indexOfFirst { it.guid == guid }

            if (alarmIndex != -1) {
                val alarmToUpdate = alarms[alarmIndex]
                val updatedAlarm = alarmToUpdate.copy(
                    active = true,
                    edited = edited
                )
                alarms[alarmIndex] = updatedAlarm
            } else {
                val alarm = Alarm(true, time.toEpochMilliseconds(), id, edited, guid)
                alarms.add(alarm)
            }

            AlarmItems(alarms)
        }

        logger.debug("Alarm $id set for $time")
    }

    override suspend fun cancelAlarm(guid: String) {
        val id = guid.hashCode()
        val alarmManager =
            APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        existingPendingIntent(id, alarmIntent(guid))?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        existingPendingIntent("countdown:$guid".hashCode(), countdownIntent(guid, 0L))?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        AlarmCountdownReceiver.hide(APPLICATION_CONTEXT, guid)

        StorageService.ALARM_ITEMS.update { alarmItems ->
            val alarms = alarmItems.alarms.toMutableList()
            val alarmIndex = alarms.indexOfFirst { it.guid == guid }

            if (alarmIndex != -1) {
                val alarmToUpdate = alarms[alarmIndex]
                val updatedAlarm = alarmToUpdate.copy(
                    active = false,
                )
                alarms[alarmIndex] = updatedAlarm
            }

            AlarmItems(alarms)
        }
        logger.debug("Alarm $id cancelled")
    }

    override fun isAlarmSet(guid: String): Boolean {
        val alarmIntent = alarmIntent(guid)
        val pendingIntent = PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            guid.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    override fun getNextAlarm(): Instant? {
        val alarmManager =
            APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.nextAlarmClock?.triggerTime?.let { Instant.fromEpochMilliseconds(it) }
    }

    /**
     * Posts the countdown notification [AlarmService.COUNTDOWN_LEAD] before the alarm, or straight
     * away when the alarm is already closer than that. Inexact on purpose - the notification is not
     * time critical and this keeps it out of the exact alarm budget
     */
    private fun scheduleCountdown(alarmManager: AlarmManager, guid: String, alarmAt: Long) {
        val showAt = alarmAt - AlarmService.COUNTDOWN_LEAD.inWholeMilliseconds
        if (showAt <= Clock.System.now().toEpochMilliseconds()) {
            AlarmCountdownReceiver.show(APPLICATION_CONTEXT, guid, alarmAt)
            return
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            showAt,
            countdownPendingIntent(guid, alarmAt)
        )
    }

    private fun alarmIntent(guid: String) =
        Intent(APPLICATION_CONTEXT, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_GUID, guid)
        }

    private fun alarmPendingIntent(guid: String): PendingIntent = PendingIntent.getBroadcast(
        APPLICATION_CONTEXT,
        guid.hashCode(),
        alarmIntent(guid),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun countdownIntent(guid: String, alarmAt: Long) =
        Intent(APPLICATION_CONTEXT, AlarmCountdownReceiver::class.java).apply {
            action = AlarmCountdownReceiver.ACTION_SHOW
            putExtra(AlarmCountdownReceiver.EXTRA_GUID, guid)
            putExtra(AlarmCountdownReceiver.EXTRA_ALARM_AT, alarmAt)
        }

    private fun countdownPendingIntent(guid: String, alarmAt: Long): PendingIntent =
        PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            "countdown:$guid".hashCode(),
            countdownIntent(guid, alarmAt),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun existingPendingIntent(requestCode: Int, intent: Intent): PendingIntent? =
        PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

    private fun launchPendingIntent(): PendingIntent? {
        val launchIntent = APPLICATION_CONTEXT.packageManager
            .getLaunchIntentForPackage(APPLICATION_CONTEXT.packageName) ?: return null

        return PendingIntent.getActivity(
            APPLICATION_CONTEXT,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.fromParts("package", APPLICATION_CONTEXT.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        APPLICATION_CONTEXT.startActivity(intent)
    }
}

actual fun initializePlatformAlarmApi(): PlatformAlarmApi {
    return AndroidAlarmApi()
}
