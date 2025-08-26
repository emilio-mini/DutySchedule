package me.emiliomini.dutyschedule.services.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import me.emiliomini.dutyschedule.ui.main.activity.MainActivity
import me.emiliomini.dutyschedule.datastore.alarm.AlarmProto
import me.emiliomini.dutyschedule.receivers.AlarmReceiver
import me.emiliomini.dutyschedule.services.storage.DataStores

object AlarmService {
    private const val TAG = "AlarmService"

    suspend fun clean() {
        val currentTimestamp = System.currentTimeMillis()

        DataStores.ALARM_ITEMS.updateData { alarmItems ->
            val updatedAlarms = alarmItems.alarmsList.filter { alarm -> currentTimestamp < alarm.timestamp }

            alarmItems.toBuilder()
                .clearAlarms()
                .addAllAlarms(updatedAlarms)
                .build()
        }
    }

    suspend fun scheduleAlarm(context: Context, timestamp: Long, code: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "Missing permission for scheduling alarms! Requesting...")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
            return
        }

        val openAppIntent = Intent(context, MainActivity::class.java)
        val pendingOpenAppIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            context,
            code,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val info = AlarmManager.AlarmClockInfo(timestamp, pendingOpenAppIntent)
        alarmManager.setAlarmClock(info, pendingAlarmIntent)

        DataStores.ALARM_ITEMS.updateData { alarmItems ->
            val alarms = alarmItems.alarmsList.toMutableList()
            val alarmIndex = alarms.indexOfFirst { it.code == code }

            if (alarmIndex != -1) {
                val alarmToUpdate = alarms[alarmIndex]
                val updatedAlarm = alarmToUpdate.toBuilder()
                    .setActive(true)
                    .build()
                alarms[alarmIndex] = updatedAlarm
            } else {
                val alarm = AlarmProto.newBuilder()
                    .setActive(true)
                    .setTimestamp(timestamp)
                    .setCode(code)
                    .build()

                alarms.add(alarm)
            }

            alarmItems.toBuilder()
                .clearAlarms()
                .addAllAlarms(alarms)
                .build()
        }

        Log.d(TAG, "Alarm $code set for $timestamp")
    }

    suspend fun cancelAlarm(context: Context, code: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            context,
            code,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingAlarmIntent)
        pendingAlarmIntent.cancel()
        Log.d(TAG, "Alarm $code cancelled")

        DataStores.ALARM_ITEMS.updateData { alarmItems ->
            val alarms = alarmItems.alarmsList.toMutableList()
            val alarmIndex = alarms.indexOfFirst { it.code == code }

            if (alarmIndex != -1) {
                val alarmToUpdate = alarms[alarmIndex]
                val updatedAlarm = alarmToUpdate.toBuilder()
                    .setActive(false)
                    .build()
                alarms[alarmIndex] = updatedAlarm
            }

            alarmItems.toBuilder()
                .clearAlarms()
                .addAllAlarms(alarms)
                .build()
        }
    }

    suspend fun deleteAlarm(context: Context, code: Int) {
        this.cancelAlarm(context, code)

        DataStores.ALARM_ITEMS.updateData { alarmItems ->
            val updatedAlarms = alarmItems.alarmsList.filter { alarm -> alarm.code != code }

            alarmItems.toBuilder()
                .clearAlarms()
                .addAllAlarms(updatedAlarms)
                .build()
        }
    }

    fun isAlarmSet(context: Context, code: Int): Boolean {
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            code,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}