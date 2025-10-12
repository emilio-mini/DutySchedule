package me.emiliomini.dutyschedule.shared.api.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)
        if (requestCode != -1) {
            NotificationActionRegistry.consume(requestCode)?.action?.invoke()
        }
    }

    companion object Companion {
        const val ACTION_PERFORM = "me.emiliomini.dutyschedule.PERFORM_NOTIFICATION_ACTION"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
    }
}