package me.emiliomini.dutyschedule.shared.api

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.services.AlarmService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService

/**
 * AlarmManager keeps no alarms across a reboot, an app update or a clock change, so every alarm has
 * to be handed back to it afterwards
 */
class AlarmBootReceiver : BroadcastReceiver() {
    private val logger = getPlatformLogger("AlarmBootReceiver")

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action !in HANDLED_ACTIONS) {
            return
        }

        APPLICATION_CONTEXT = context.applicationContext
        logger.debug("Re-registering alarms after $action")

        val pending = goAsync()
        scope.launch {
            try {
                StorageService.initialize()
                AlarmService.rescheduleStoredAlarms()
            } catch (e: Exception) {
                logger.error("Could not re-register alarms after $action", throwable = e)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )

        private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}
