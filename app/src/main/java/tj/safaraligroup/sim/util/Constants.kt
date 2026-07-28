package tj.safaraligroup.sim.util

object Constants {

    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CHATS = "chats"
    const val COLLECTION_MESSAGES = "messages"
    const val COLLECTION_TOKENS = "tokens"

    // Agora (get your own App ID from https://console.agora.io)
    // 🔴 IMPORTANT: Replace with your own Agora App ID
    const val AGORA_APP_ID = "YOUR_AGORA_APP_ID_HERE"

    // Notification Channels
    const val CHANNEL_CALLS = "call_channel"
    const val CHANNEL_MESSAGES = "message_channel"

    // Shared Preferences
    const val PREF_NAME = "imo_clone_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_PHONE = "phone_number"
}
