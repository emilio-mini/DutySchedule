package me.emiliomini.dutyschedule.services.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import me.emiliomini.dutyschedule.R

class AlarmSoundService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate();
        val alarmUri = "content://settings/system/alarm_alert".toUri();
        mediaPlayer = MediaPlayer.create(this, alarmUri);
        mediaPlayer.isLooping = true;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification();
        startForeground(1, notification);
        mediaPlayer.start();

        return START_STICKY;
    }

    override fun onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private fun createNotification(): Notification {
        val channelId = "duty_alarms";
        val channelName = "Duty Alarm Notifications";

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
        manager.createNotificationChannel(channel);

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Duty Alarm")
            .setContentText("It's time!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build();
    }
}