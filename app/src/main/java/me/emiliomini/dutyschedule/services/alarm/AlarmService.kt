package me.emiliomini.dutyschedule.services.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import me.emiliomini.dutyschedule.MainActivity
import me.emiliomini.dutyschedule.receiver.AlarmReceiver

object AlarmService {
    private const val TAG = "AlarmService"

    fun scheduleAlarm(context: Context, timestamp: Long, code: Int) {
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
        Log.d(TAG, "Alarm $code set for $timestamp")
    }

    fun cancelAlarm(context: Context, code: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingAlarmIntent = PendingIntent.getBroadcast(
            context,
            code,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingAlarmIntent)
        Log.d(TAG, "Alarm $code cancelled")
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