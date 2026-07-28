package tj.safaraligroup.sim.ui.call

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tj.safaraligroup.sim.data.repository.ChatRepository
import tj.safaraligroup.sim.databinding.ActivityVoiceCallBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoiceCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCallBinding
    private var channelName: String = ""
    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var isMuted: Boolean = false
    private var isSpeakerOn: Boolean = true
    private var callStartTime: Long = 0L
    private var callDuration: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        channelName = intent.getStringExtra("channelName") ?: ""
        otherUserId = intent.getStringExtra("otherUserId") ?: ""
        otherUserName = intent.getStringExtra("otherUserName") ?: "User"

        binding.tvContactName.text = otherUserName
        callStartTime = System.currentTimeMillis()

        setupAgoraEngine()
        setupCallButtons()
    }

    private fun setupAgoraEngine() {
        // 🔴 TODO: Implement with Agora SDK
        // 1. Create RtcEngine with Agora App ID
        // 2. Join channel with channelName
        // 3. Start audio call
        //
        // For production, you need:
        // - Agora App ID from https://console.agora.io
        // - Valid token (if enabled)
        //
        // RtcEngine.create(this, AGORA_APP_ID, object : IRtcEngineEventHandler() {
        //     override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {}
        //     override fun onUserOffline(uid: Int, reason: Int) { hangUp() }
        // })
    }

    private fun setupCallButtons() {
        binding.btnMute.setOnClickListener {
            isMuted = !isMuted
            binding.btnMute.setImageResource(
                if (isMuted) tj.safaraligroup.sim.R.drawable.ic_mic_off
                else tj.safaraligroup.sim.R.drawable.ic_mic
            )
            // RtcEngine.muteLocalAudioStream(isMuted)
        }

        binding.btnSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            binding.btnSpeaker.setImageResource(
                if (isSpeakerOn) tj.safaraligroup.sim.R.drawable.ic_volume_up
                else tj.safaraligroup.sim.R.drawable.ic_volume_down
            )
            // RtcEngine.setEnableSpeakerphone(isSpeakerOn)
        }

        binding.btnEndCall.setOnClickListener {
            hangUp()
        }
    }

    private fun hangUp() {
        callDuration = (System.currentTimeMillis() - callStartTime) / 1000

        // Save call message
        val prefs = getSharedPreferences("imo_clone_prefs", MODE_PRIVATE)
        val chatRepo = ChatRepository(
            currentUserId = prefs.getString("user_id", "") ?: ""
        )

        CoroutineScope(Dispatchers.IO).launch {
            chatRepo.sendCallMessage(
                chatId = channelName,
                receiverId = otherUserId,
                callType = tj.safaraligroup.sim.data.model.Message.TYPE_CALL_END,
                callDuration = callDuration
            )
        }

        // Destroy Agora engine
        // RtcEngine.destroy()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // RtcEngine.destroy()
    }
}
