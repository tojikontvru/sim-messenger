package tj.safaraligroup.sim.data.model

data class User(
    val uid: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val status: String = "Hey! I'm using IMO Clone",
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val fcmToken: String = "",
    val contacts: List<String> = emptyList()
)
