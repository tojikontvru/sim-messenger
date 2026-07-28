package tj.safaraligroup.sim.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import tj.safaraligroup.sim.data.model.User
import tj.safaraligroup.sim.data.repository.AuthRepository
import tj.safaraligroup.sim.data.repository.ChatRepository
import tj.safaraligroup.sim.databinding.ActivityNewChatBinding
import kotlinx.coroutines.launch

class NewChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewChatBinding
    private lateinit var authRepo: AuthRepository
    private lateinit var chatRepo: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("imo_clone_prefs", MODE_PRIVATE)
        authRepo = AuthRepository(prefs = prefs)
        chatRepo = ChatRepository(currentUserId = authRepo.currentUserId)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSearch.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.length < 10) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchUser(phone)
        }
    }

    private fun searchUser(phone: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnSearch.isEnabled = false

        lifecycleScope.launch {
            val users = authRepo.searchUsersByPhone(phone)
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnSearch.isEnabled = true

            if (users.isEmpty()) {
                Toast.makeText(this@NewChatActivity, "No user found with this number", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val foundUser = users.first()

            // Don't allow chatting with yourself
            if (foundUser.uid == authRepo.currentUserId) {
                Toast.makeText(this@NewChatActivity, "You can't chat with yourself", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Create or get chat
            val chatId = chatRepo.createOrGetChat(foundUser.uid)
            val intent = Intent(this@NewChatActivity, ChatActivity::class.java).apply {
                putExtra("chatId", chatId)
                putExtra("otherUserId", foundUser.uid)
            }
            startActivity(intent)
            finish()
        }
    }
}
