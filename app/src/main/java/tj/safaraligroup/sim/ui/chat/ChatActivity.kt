package tj.safaraligroup.sim.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import tj.safaraligroup.sim.data.model.Chat
import tj.safaraligroup.sim.data.model.Message
import tj.safaraligroup.sim.data.model.User
import tj.safaraligroup.sim.data.repository.AuthRepository
import tj.safaraligroup.sim.data.repository.ChatRepository
import tj.safaraligroup.sim.databinding.ActivityChatBinding
import tj.safaraligroup.sim.ui.call.VideoCallActivity
import tj.safaraligroup.sim.ui.call.VoiceCallActivity
import tj.safaraligroup.sim.ui.components.MessageAdapter
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var authRepo: AuthRepository
    private lateinit var chatRepo: ChatRepository
    private lateinit var messageAdapter: MessageAdapter
    private var chatId: String = ""
    private var otherUserId: String = ""
    private var otherUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId = intent.getStringExtra("chatId") ?: ""
        otherUserId = intent.getStringExtra("otherUserId") ?: ""

        val prefs = getSharedPreferences("imo_clone_prefs", MODE_PRIVATE)
        authRepo = AuthRepository(prefs = prefs)
        chatRepo = ChatRepository(currentUserId = authRepo.currentUserId)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        loadOtherUser()
        observeMessages()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(authRepo.currentUserId)
        binding.recyclerMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }

        binding.btnVoiceCall.setOnClickListener {
            val intent = Intent(this, VoiceCallActivity::class.java).apply {
                putExtra("channelName", chatId)
                putExtra("otherUserId", otherUserId)
                putExtra("otherUserName", otherUser?.displayName ?: "User")
            }
            startActivity(intent)
        }

        binding.btnVideoCall.setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java).apply {
                putExtra("channelName", chatId)
                putExtra("otherUserId", otherUserId)
                putExtra("otherUserName", otherUser?.displayName ?: "User")
            }
            startActivity(intent)
        }
    }

    private fun loadOtherUser() {
        lifecycleScope.launch {
            otherUser = authRepo.getUser(otherUserId)
            otherUser?.let { user ->
                binding.toolbar.title = user.displayName
                binding.tvStatus.text = if (user.isOnline) "Online" else "Offline"
                Glide.with(this@ChatActivity)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .placeholder(tj.safaraligroup.sim.R.drawable.ic_person)
                    .into(binding.ivUserAvatar)
            }
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            chatRepo.getMessages(chatId).collect { messages ->
                messageAdapter.submitList(messages)
                binding.recyclerMessages.scrollToPosition(messages.size - 1)
                binding.tvEmptyChat.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE

                // Mark as seen
                messages.lastOrNull { it.senderId == otherUserId && !it.isSeen }?.let {
                    chatRepo.markAsSeen(chatId, otherUserId)
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        binding.etMessage.setText("")
        lifecycleScope.launch {
            chatRepo.sendMessage(chatId, otherUserId, text)
        }
    }
}
