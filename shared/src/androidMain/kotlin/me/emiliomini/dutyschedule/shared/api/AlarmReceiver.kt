package me.emiliomini.dutyschedule.shared.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        APPLICATION_CONTEXT = context.applicationContext

        val guid = intent?.getStringExtra(EXTRA_GUID)
        if (guid != null) {
            AlarmCountdownReceiver.hide(context, guid)
        }

        val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
            putExtra(EXTRA_GUID, guid)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_GUID = "guid"
    }
}
