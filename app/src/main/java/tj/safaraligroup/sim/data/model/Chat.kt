package tj.safaraligroup.sim.data.model

data class Chat(
    val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val lastSenderId: String = "",
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val groupName: String = "",
    val groupImageUrl: String = "",
    val createdBy: String = ""
) {
    /**
     * Get the other participant's ID (for 1-on-1 chats)
     */
    fun getOtherUserId(currentUserId: String): String {
        return participants.firstOrNull { it != currentUserId } ?: ""
    }
}
