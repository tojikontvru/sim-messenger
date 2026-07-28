package tj.safaraligroup.sim.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import tj.safaraligroup.sim.data.model.User
import tj.safaraligroup.sim.data.repository.AuthRepository
import tj.safaraligroup.sim.databinding.ActivityPhoneAuthBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding
    private val auth = Firebase.auth
    private var verificationId: String = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val defaultCountryCode = "+992"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSendCode.setOnClickListener {
            val rawPhone = binding.etPhoneNumber.text.toString().trim()
            if (rawPhone.isEmpty()) {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Normalize phone number
            val phoneNumber = normalizePhoneNumber(rawPhone)
            if (phoneNumber.length < 10) {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendVerificationCode(phoneNumber)
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.length != 6) {
                Toast.makeText(this, "Enter the 6-digit code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(code)
        }
    }

    /**
     * Normalize phone number to +XXXXXXXXXX format
     * Handles: 912345678, +992912345678, 00992912345678, 8912345678
     */
    private fun normalizePhoneNumber(raw: String): String {
        var number = raw
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        // Already has +
        if (number.startsWith("+")) return number

        // Starts with 00 → replace with +
        if (number.startsWith("00")) {
            number = "+${number.drop(2)}"
            // If +992 then it's Tajikistan
            return number
        }

        // Tajikistan format: 9XXXXXXXXX (9 digits starting with 9)
        if (number.matches(Regex("^9\\d{8}$"))) {
            return "+992$number"
        }

        // Tajikistan format with leading 8: 89XXXXXXXXX (10 digits starting with 8)
        if (number.matches(Regex("^89\\d{8}$"))) {
            return "+992${number.drop(1)}"
        }

        // If starts with 9X but only 9 digits total → local format
        if (number.matches(Regex("^9\\d{7}$"))) {
            return "+992$number"
        }

        // If starts with 09X → remove leading 0
        if (number.startsWith("09")) {
            number = number.drop(1) // remove just the 0
            return "+992$number"
        }

        // Otherwise just prepend +
        return "+$number"
    }

    private fun sendVerificationCode(phoneNumber: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendCode.isEnabled = false

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(120L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            binding.progressBar.visibility = View.GONE
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            binding.progressBar.visibility = View.GONE
            binding.btnSendCode.isEnabled = true

            val msg = when {
                e.message?.contains("Too many") == true -> {
                    "Too many attempts. Please try again later."
                }
                e.message?.contains("invalid") == true -> {
                    "Invalid phone number. Check the number and try again."
                }
                else -> "Verification failed: ${e.message}"
            }
            Toast.makeText(this@PhoneAuthActivity, msg, Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            this@PhoneAuthActivity.verificationId = verificationId
            this@PhoneAuthActivity.resendToken = token

            // Switch to code input layout
            binding.layoutPhone.visibility = View.GONE
            binding.layoutCode.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.btnSendCode.isEnabled = true
        }
    }

    private fun verifyCode(code: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnVerify.isEnabled = false

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = getSharedPreferences("tj_safaraligroup_sim", MODE_PRIVATE)
            val authRepo = AuthRepository(prefs = prefs, firebaseAuth = auth)

            try {
                val result = authRepo.signInWithCredential(credential)

                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true

                    result.onSuccess { firebaseUser ->
                        // Check if user profile exists in Firestore
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = authRepo.getUser(firebaseUser.uid)
                            withContext(Dispatchers.Main) {
                                if (user == null || user.displayName.isEmpty()) {
                                    // New user → go to profile setup
                                    val intent = Intent(
                                        this@PhoneAuthActivity,
                                        ProfileSetupActivity::class.java
                                    )
                                    intent.putExtra("phone", firebaseUser.phoneNumber)
                                    startActivity(intent)
                                } else {
                                    // Existing user → go to chat list
                                    val intent = Intent(
                                        this@PhoneAuthActivity,
                                        tj.safaraligroup.sim.ui.chat.ChatListActivity::class.java
                                    )
                                    startActivity(intent)
                                }
                                finish()
                            }
                        }
                    }.onFailure { e ->
                        Toast.makeText(
                            this@PhoneAuthActivity,
                            "Sign-in failed: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true
                    Toast.makeText(
                        this@PhoneAuthActivity,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}