package tj.safaraligroup.sim.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import tj.safaraligroup.sim.R
import tj.safaraligroup.sim.ui.call.VoiceCallActivity
import tj.safaraligroup.sim.ui.call.VideoCallActivity
import tj.safaraligroup.sim.ui.chat.ChatActivity
import tj.safaraligroup.sim.util.Constants

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token updated — save to Firestore via AuthRepository
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data.let { data ->
            val type = data["type"] ?: return

            when (type) {
                "message" -> showMessageNotification(
                    data["senderName"] ?: "User",
                    data["text"] ?: "New message",
                    data["chatId"] ?: "",
                    data["senderId"] ?: ""
                )
                "call" -> showIncomingCallNotification(
                    data["callType"] ?: "voice",
                    data["callerName"] ?: "User",
                    data["channelName"] ?: "",
                    data["callerId"] ?: ""
                )
            }
        }
    }

    private fun showMessageNotification(senderName: String, text: String, chatId: String, senderId: String) {
        createNotificationChannels()

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("otherUserId", senderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(senderName)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(senderId.hashCode(), notification)
    }

    private fun showIncomingCallNotification(
        callType: String,
        callerName: String,
        channelName: String,
        callerId: String
    ) {
        createNotificationChannels()

        val intent = if (callType == "video") {
            Intent(this, VideoCallActivity::class.java)
        } else {
            Intent(this, VoiceCallActivity::class.java)
        }.apply {
            putExtra("channelName", channelName)
            putExtra("otherUserId", callerId)
            putExtra("otherUserName", callerName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_CALLS)
            .setSmallIcon(if (callType == "video") R.drawable.ic_videocam else R.drawable.ic_call)
            .setContentTitle("Incoming ${if (callType == "video") "Video" else "Voice"} Call")
            .setContentText("$callerName is calling you...")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .build()

        NotificationManagerCompat.from(this).notify(CALL_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messageChannel = NotificationChannel(
                Constants.CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
            }

            val callChannel = NotificationChannel(
                Constants.CHANNEL_CALLS,
                "Calls",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Incoming call notifications"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(messageChannel)
            manager.createNotificationChannel(callChannel)
        }
    }

    companion object {
        private const val CALL_NOTIFICATION_ID = 2001
    }
}
