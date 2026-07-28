package tj.safaraligroup.sim.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import tj.safaraligroup.sim.data.model.Chat
import tj.safaraligroup.sim.data.model.Message
import tj.safaraligroup.sim.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val currentUserId: String
) {

    /**
     * Create or get existing 1-on-1 chat
     */
    suspend fun createOrGetChat(otherUserId: String): String {
        // Check if chat already exists
        val existingChats = firestore.collection(Constants.COLLECTION_CHATS)
            .whereArrayContains("participants", currentUserId)
            .get()
            .await()

        val existingChat = existingChats.documents.firstOrNull { doc ->
            val participants = doc.get("participants") as? List<*> ?: emptyList<Any>()
            participants.contains(otherUserId) && !(doc.getBoolean("isGroup") ?: false)
        }

        if (existingChat != null) {
            return existingChat.id
        }

        // Create new chat
        val chatId = UUID.randomUUID().toString()
        val chat = Chat(
            chatId = chatId,
            participants = listOf(currentUserId, otherUserId),
            isGroup = false
        )

        firestore.collection(Constants.COLLECTION_CHATS)
            .document(chatId)
            .set(chat)
            .await()

        return chatId
    }

    /**
     * Send a text message
     */
    suspend fun sendMessage(chatId: String, receiverId: String, text: String): Result<Message> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val message = Message(
                messageId = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                chatId = chatId,
                text = text,
                messageType = Message.TYPE_TEXT,
                timestamp = System.currentTimeMillis(),
                isDelivered = true
            )

            // Save message
            firestore.collection(Constants.COLLECTION_MESSAGES)
                .document(messageId)
                .set(message)
                .await()

            // Update chat's last message
            firestore.collection(Constants.COLLECTION_CHATS)
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to message.timestamp,
                        "lastSenderId" to currentUserId
                    )
                )
                .await()

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a call notification message
     */
    suspend fun sendCallMessage(
        chatId: String,
        receiverId: String,
        callType: String,
        callDuration: Long = 0L
    ): Result<Message> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val message = Message(
                messageId = messageId,
                senderId = currentUserId,
                receiverId = receiverId,
                chatId = chatId,
                text = when (callType) {
                    Message.TYPE_CALL_START -> "📞 Voice call"
                    Message.TYPE_CALL_END -> "📞 Call ended ($callDuration sec)"
                    Message.TYPE_CALL_MISSED -> "📞 Missed call"
                    else -> "📞 Call"
                },
                messageType = callType,
                timestamp = System.currentTimeMillis(),
                callDuration = callDuration
            )

            firestore.collection(Constants.COLLECTION_MESSAGES)
                .document(messageId)
                .set(message)
                .await()

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user's chats as Flow (real-time)
     */
    fun getChats(): Flow<List<Chat>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_CHATS)
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val chats = snapshot?.documents?.mapNotNull {
                    it.toObject(Chat::class.java)
                } ?: emptyList()

                trySend(chats)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get messages for a chat as Flow (real-time)
     */
    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_MESSAGES)
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(Message::class.java)
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Mark messages as seen
     */
    suspend fun markAsSeen(chatId: String, senderId: String) {
        try {
            val messages = firestore.collection(Constants.COLLECTION_MESSAGES)
                .whereEqualTo("chatId", chatId)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("isSeen", false)
                .get()
                .await()

            val batch = firestore.batch()
            messages.documents.forEach { doc ->
                batch.update(doc.reference, "isSeen", true)
            }
            batch.commit().await()
        } catch (_: Exception) {}
    }
}
