package tj.safaraligroup.sim.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import tj.safaraligroup.sim.R
import tj.safaraligroup.sim.ui.call.VoiceCallActivity
import tj.safaraligroup.sim.ui.call.VideoCallActivity
import tj.safaraligroup.sim.util.Constants

class CallService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelName = intent?.getStringExtra("channelName") ?: ""
        val isVideo = intent?.getBooleanExtra("isVideo", false) ?: false

        val notification = createOngoingNotification(channelName, isVideo)
        startForeground(CALL_FOREGROUND_ID, notification)

        // 🔴 TODO: Initialize Agora connection here

        return START_NOT_STICKY
    }

    private fun createOngoingNotification(channelName: String, isVideo: Boolean): Notification {
        val intent = if (isVideo) {
            Intent(this, VideoCallActivity::class.java)
        } else {
            Intent(this, VoiceCallActivity::class.java)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.CHANNEL_CALLS)
            .setContentTitle(if (isVideo) "Video Call" else "Voice Call")
            .setContentText("Call in progress...")
            .setSmallIcon(if (isVideo) R.drawable.ic_videocam else R.drawable.ic_call)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // 🔴 TODO: Leave Agora channel
    }

    companion object {
        private const val CALL_FOREGROUND_ID = 3001
    }
}
