package me.emiliomini.dutyschedule.shared.api

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.notifications_alarms_duty_action_dismiss
import dutyschedule.shared.generated.resources.notifications_alarms_duty_content
import dutyschedule.shared.generated.resources.notifications_alarms_duty_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.emiliomini.dutyschedule.shared.R
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import me.emiliomini.dutyschedule.shared.services.prep.live.PrepService
import org.jetbrains.compose.resources.getString


class AlarmSoundService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var ringtonePlayer: Ringtone

    companion object {
        const val ACTION_STOP_SOUND = "me.emiliomini.dutyschedule.services.alarm.ACTION_STOP_SOUND"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        APPLICATION_CONTEXT = this.applicationContext

        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtonePlayer = RingtoneManager.getRingtone(this, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtonePlayer.isLooping = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SOUND) {
            stopSelf()
            return START_NOT_STICKY
        }

        (getPlatformNotificationApi() as AndroidNotificationApi).verifyOrCreateChannel(
            NotificationChannelMapping.ALARMS
        )

        val notification = createNotification()
        startForeground(1, notification)
        ringtonePlayer.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        ringtonePlayer.play()
        serviceScope.launch {
            PrepService.loadUpcoming()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::ringtonePlayer.isInitialized) {
            if (ringtonePlayer.isPlaying) {
                ringtonePlayer.stop()
            }
        }
        serviceScope.cancel()
    }

    private fun createNotification(): Notification {
        val stopSoundIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = ACTION_STOP_SOUND
        }
        val stopSoundPendingIntent: PendingIntent =
            PendingIntent.getService(
                this,
                0,
                stopSoundIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val title = runBlocking {
            getString(Res.string.notifications_alarms_duty_title)
        }
        val content = runBlocking {
            getString(Res.string.notifications_alarms_duty_content)
        }
        val dismiss = runBlocking {
            getString(Res.string.notifications_alarms_duty_action_dismiss)
        }

        return NotificationCompat.Builder(this, NotificationChannelMapping.ALARMS.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .addAction(
                0,
                dismiss,
                stopSoundPendingIntent
            )
            .setSilent(true)
            .setDeleteIntent(stopSoundPendingIntent)
            .build()
    }
}