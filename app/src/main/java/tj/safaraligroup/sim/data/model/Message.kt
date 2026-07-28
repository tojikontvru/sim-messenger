package tj.safaraligroup.sim.data.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val chatId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val messageType: String = TYPE_TEXT, // TEXT, IMAGE, CALL_START, CALL_END
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val isDelivered: Boolean = false,
    val callDuration: Long = 0L // for call messages
) {
    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"
        const val TYPE_CALL_START = "call_start"
        const val TYPE_CALL_END = "call_end"
        const val TYPE_CALL_MISSED = "call_missed"
    }
}
