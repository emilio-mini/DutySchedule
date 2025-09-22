package me.emiliomini.dutyschedule.shared.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, AlarmSoundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}