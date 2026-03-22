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
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AndroidAlarmApi : PlatformAlarmApi {
    private val logger = getPlatformLogger("AndroidAlarmApi")

    override fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isPermissionGranted()) {
            val intent =
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", APPLICATION_CONTEXT.packageName, null)
                }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            APPLICATION_CONTEXT.startActivity(intent)
            return isPermissionGranted()
        } else return true
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

        if (time.toEpochMilliseconds() < Clock.System.now().toEpochMilliseconds()) {
            return
        }

        val id = guid.hashCode()
        val alarmManager =
            APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", APPLICATION_CONTEXT.packageName, null)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            APPLICATION_CONTEXT.startActivity(intent)
            return
        }

        val alarmIntent = Intent(APPLICATION_CONTEXT, AlarmReceiver::class.java)
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            id,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val info = AlarmManager.AlarmClockInfo(time.toEpochMilliseconds(), pendingAlarmIntent)
        alarmManager.setAlarmClock(info, pendingAlarmIntent)

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

        logger.d("Alarm $id set")
    }

    override suspend fun cancelAlarm(guid: String) {
        val id = guid.hashCode()
        val alarmManager =
            APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(APPLICATION_CONTEXT, AlarmReceiver::class.java)
        alarmIntent.extras?.putString("guid", guid)
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            id,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingAlarmIntent)
        pendingAlarmIntent.cancel()

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
        logger.d("Alarm $id cancelled")
    }

    override fun isAlarmSet(guid: String): Boolean {
        val id = guid.hashCode()
        val alarmIntent = Intent(APPLICATION_CONTEXT, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            id,
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

}

actual fun initializePlatformAlarmApi(): PlatformAlarmApi {
    return AndroidAlarmApi()
}
