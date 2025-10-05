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
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AndroidAlarmApi : PlatformAlarmApi {
    private val logger = getPlatformLogger("AndroidAlarmApi")

    override suspend fun setAlarm(
        id: Int,
        time: Instant,
        zone: TimeZone
    ) {
        val alarmManager = APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", APPLICATION_CONTEXT.packageName, null)
            }
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
            val alarmIndex = alarms.indexOfFirst { it.code == id }

            if (alarmIndex != -1) {
                val alarmToUpdate = alarms[alarmIndex]
                val updatedAlarm = alarmToUpdate.copy(active = true)
                alarms[alarmIndex] = updatedAlarm
            } else {
                val alarm = Alarm(true, time.toEpochMilliseconds(), id)
                alarms.add(alarm)
            }

            AlarmItems(alarms)
        }

        logger.d("Alarm $id set")
    }

    override suspend fun cancelAlarm(id: Int) {
        val alarmManager = APPLICATION_CONTEXT.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(APPLICATION_CONTEXT, AlarmReceiver::class.java)
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
            val alarmIndex = alarms.indexOfFirst { it.code == id }

            if (alarmIndex != -1) {
                val alarmToUpdate = alarms[alarmIndex]
                val updatedAlarm = alarmToUpdate.copy(active = false)
                alarms[alarmIndex] = updatedAlarm
            }

            AlarmItems(alarms)
        }
        logger.d("Alarm $id cancelled")
    }

    override fun isAlarmSet(id: Int): Boolean {
        val alarmIntent = Intent(APPLICATION_CONTEXT, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            APPLICATION_CONTEXT,
            id,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

}

actual fun initializePlatformAlarmApi(): PlatformAlarmApi {
    return AndroidAlarmApi()
}
