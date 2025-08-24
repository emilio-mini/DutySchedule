package me.emiliomini.dutyschedule.services.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.enums.AppNotificationChannel

class AlarmSoundService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        const val ACTION_STOP_SOUND = "me.emiliomini.dutyschedule.services.alarm.ACTION_STOP_SOUND"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mediaPlayer.setAudioAttributes(audioAttributes)

        val alarmUri = "content://settings/system/alarm_alert".toUri()
        mediaPlayer.setDataSource(this, alarmUri)
        mediaPlayer.prepare()
        mediaPlayer.isLooping = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SOUND) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification()
        startForeground(1, notification)
        mediaPlayer.start()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }

    private fun createNotification(): Notification {
        val stopSoundIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = ACTION_STOP_SOUND
        }
        val stopSoundPendingIntent: PendingIntent =
            PendingIntent.getService(this, 0, stopSoundIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, AppNotificationChannel.ALARMS.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notifications_alarms_duty_title))
            .setContentText(getString(R.string.notifications_alarms_duty_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(0,
                getString(R.string.notifications_alarms_duty_action_dismiss), stopSoundPendingIntent)
            .setDeleteIntent(stopSoundPendingIntent)
            .build()
    }
}