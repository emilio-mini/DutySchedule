package me.emiliomini.dutyschedule.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import me.emiliomini.dutyschedule.services.alarm.AlarmSoundService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, AlarmSoundService::class.java);
        serviceIntent.putExtra("ALARM_MESSAGE", "It's time!");
        ContextCompat.startForegroundService(context, serviceIntent);
    }

}