package tj.safaraligroup.sim.ui.call

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import tj.safaraligroup.sim.data.repository.ChatRepository
import tj.safaraligroup.sim.databinding.ActivityVideoCallBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    private var channelName: String = ""
    private var otherUserId: String = ""
    private var otherUserName: String = ""
    private var isMuted: Boolean = false
    private var isCameraOff: Boolean = false
    private var isSpeakerOn: Boolean = true
    private var callStartTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
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
        // 2. Enable video
        // 3. Join channel
        // 4. Set up local and remote video views
        //
        // val config = RtcEngineConfig().apply {
        //     mContext = this@VideoCallActivity
        //     mAppId = Constants.AGORA_APP_ID
        //     mEventHandler = object : IRtcEngineEventHandler() {
        //         override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {}
        //         override fun onUserJoined(uid: Int, elapsed: Int) {
        //             val surfaceView = RtcEngine.CreateRendererView(this@VideoCallActivity)
        //             RtcEngine.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
        //             binding.remoteVideoContainer.addView(surfaceView)
        //         }
        //         override fun onUserOffline(uid: Int, reason: Int) { hangUp() }
        //     }
        // }
        // RtcEngine.create(config)
        // RtcEngine.enableVideo()
        // RtcEngine.startPreview()
        // val localView = RtcEngine.CreateRendererView(this)
        // RtcEngine.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        // binding.localVideoContainer.addView(localView)
        // RtcEngine.joinChannel(token, channelName, null, 0)
    }

    private fun setupCallButtons() {
        binding.btnMute.setOnClickListener {
            isMuted = !isMuted
            binding.btnMute.setImageResource(
                if (isMuted) tj.safaraligroup.sim.R.drawable.ic_mic_off
                else tj.safaraligroup.sim.R.drawable.ic_mic
            )
        }

        binding.btnSwitchCamera.setOnClickListener {
            // RtcEngine.switchCamera()
        }

        binding.btnCamera.setOnClickListener {
            isCameraOff = !isCameraOff
            binding.btnCamera.setImageResource(
                if (isCameraOff) tj.safaraligroup.sim.R.drawable.ic_videocam_off
                else tj.safaraligroup.sim.R.drawable.ic_videocam
            )
            // RtcEngine.muteLocalVideoStream(isCameraOff)
        }

        binding.btnEndCall.setOnClickListener {
            hangUp()
        }
    }

    private fun hangUp() {
        val callDuration = (System.currentTimeMillis() - callStartTime) / 1000

        val prefs = getSharedPreferences("imo_clone_prefs", MODE_PRIVATE)
        val chatRepo = ChatRepository(currentUserId = prefs.getString("user_id", "") ?: "")

        CoroutineScope(Dispatchers.IO).launch {
            chatRepo.sendCallMessage(
                chatId = channelName,
                receiverId = otherUserId,
                callType = tj.safaraligroup.sim.data.model.Message.TYPE_CALL_END,
                callDuration = callDuration
            )
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // RtcEngine.destroy()
    }
}
