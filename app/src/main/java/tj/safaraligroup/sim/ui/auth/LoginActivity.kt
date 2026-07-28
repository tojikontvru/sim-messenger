package tj.safaraligroup.sim.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import tj.safaraligroup.sim.databinding.ActivityLoginBinding
import tj.safaraligroup.sim.ui.chat.ChatListActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            navigateToChatList()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            val intent = Intent(this, PhoneAuthActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToChatList() {
        val intent = Intent(this, ChatListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
