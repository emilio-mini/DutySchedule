@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import android.app.PendingIntent
import android.content.Intent
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AndroidAlarmApi : PlatformAlarmApi {
    override suspend fun setAlarm(
        id: Int,
        time: Instant,
        zone: TimeZone
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun cancelAlarm(id: Int) {
        TODO("Not yet implemented")
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
