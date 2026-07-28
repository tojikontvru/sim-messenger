package tj.safaraligroup.sim.data.repository

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import tj.safaraligroup.sim.data.model.User
import tj.safaraligroup.sim.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val prefs: SharedPreferences
) {

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: prefs.getString(Constants.PREF_USER_ID, "") ?: ""

    /**
     * Get auth state as a Flow
     */
    fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    /**
     * Sign in with phone auth credential
     */
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user!!

            // Save to prefs
            prefs.edit()
                .putString(Constants.PREF_USER_ID, user.uid)
                .putString(Constants.PREF_PHONE, user.phoneNumber ?: "")
                .apply()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create or update user profile in Firestore
     */
    suspend fun createUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user by ID
     */
    suspend fun getUser(userId: String): User? {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Search users by phone number
     */
    suspend fun searchUsersByPhone(phoneNumber: String): List<User> {
        return try {
            val result = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .await()
            result.documents.mapNotNull { it.toObject(User::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Update FCM token
     */
    suspend fun updateFcmToken(token: String) {
        val uid = currentUserId
        if (uid.isNotEmpty()) {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update("fcmToken", token)
                .await()
        }
    }

    /**
     * Update online status
     */
    suspend fun updateOnlineStatus(isOnline: Boolean) {
        val uid = currentUserId
        if (uid.isNotEmpty()) {
            firestore.collection(Constants.COLLECTION_USERS)
                .document(uid)
                .update(
                    mapOf(
                        "isOnline" to isOnline,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
                .await()
        }
    }

    /**
     * Logout
     */
    fun logout() {
        prefs.edit().clear().apply()
        firebaseAuth.signOut()
    }
}
