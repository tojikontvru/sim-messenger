package tj.safaraligroup.sim.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import tj.safaraligroup.sim.data.model.User
import tj.safaraligroup.sim.data.repository.AuthRepository
import tj.safaraligroup.sim.databinding.ActivityProfileSetupBinding
import tj.safaraligroup.sim.ui.chat.ChatListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSetupBinding
    private var profileImageUri: Uri? = null
    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        binding.btnContinue.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveProfile(name)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            profileImageUri = data?.data
            binding.ivProfile.setImageURI(profileImageUri)
        }
    }

    private fun saveProfile(name: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnContinue.isEnabled = false

        val prefs = getSharedPreferences("imo_clone_prefs", MODE_PRIVATE)
        val authRepo = AuthRepository(prefs = prefs)
        val uid = authRepo.currentUserId

        if (uid.isEmpty()) {
            Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            var imageUrl = ""

            // Upload profile image if selected
            profileImageUri?.let { uri ->
                try {
                    val ref = storage.reference.child("profile_images/$uid/${UUID.randomUUID()}.jpg")
                    ref.putFile(uri).await()
                    imageUrl = ref.downloadUrl.await().toString()
                } catch (_: Exception) {}
            }

            // Save user profile
            val user = User(
                uid = uid,
                phoneNumber = authRepo.currentUser?.phoneNumber ?: "",
                displayName = name,
                profileImageUrl = imageUrl
            )

            val result = authRepo.createUserProfile(user)

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnContinue.isEnabled = true

                result.onSuccess {
                    val intent = Intent(this@ProfileSetupActivity, ChatListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@ProfileSetupActivity, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
}
