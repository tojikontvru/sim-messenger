package tj.safaraligroup.sim.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import tj.safaraligroup.sim.data.model.Chat
import tj.safaraligroup.sim.data.repository.AuthRepository
import tj.safaraligroup.sim.data.repository.ChatRepository
import tj.safaraligroup.sim.databinding.ActivityChatListBinding
import tj.safaraligroup.sim.ui.auth.LoginActivity
import tj.safaraligroup.sim.ui.call.VoiceCallActivity
import tj.safaraligroup.sim.ui.components.ChatAdapter
import kotlinx.coroutines.launch

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private lateinit var authRepo: AuthRepository
    private lateinit var chatRepo: ChatRepository
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("imo_clone_prefs", MODE_PRIVATE)
        authRepo = AuthRepository(prefs = prefs)

        if (authRepo.currentUser == null) {
            navigateToLogin()
            return
        }

        chatRepo = ChatRepository(currentUserId = authRepo.currentUserId)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeChats()
        setupFCM()
    }

    private fun setupToolbar() {
        binding.toolbar.title = "IMO Clone"
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                tj.safaraligroup.sim.R.id.action_logout -> {
                    authRepo.logout()
                    navigateToLogin()
                    true
                }
                tj.safaraligroup.sim.R.id.action_new_chat -> {
                    startActivity(Intent(this, NewChatActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter { chat ->
            openChat(chat)
        }
        binding.recyclerChats.apply {
            layoutManager = LinearLayoutManager(this@ChatListActivity)
            adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            // Manual refresh (Firebase auto-updates)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun observeChats() {
        lifecycleScope.launch {
            chatRepo.getChats().collect { chats ->
                chatAdapter.submitList(chats)
                binding.tvEmpty.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun openChat(chat: Chat) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatId", chat.chatId)
            putExtra("otherUserId", chat.getOtherUserId(authRepo.currentUserId))
        }
        startActivity(intent)
    }

    private fun setupFCM() {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                lifecycleScope.launch {
                    authRepo.updateFcmToken(token)
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            authRepo.updateOnlineStatus(true)
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            authRepo.updateOnlineStatus(false)
        }
    }
}
