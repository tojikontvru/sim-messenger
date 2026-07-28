package tj.safaraligroup.sim.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import tj.safaraligroup.sim.data.model.Chat
import tj.safaraligroup.sim.databinding.ItemChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private val onChatClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: Chat) {
            binding.tvName.text = "Chat" // TODO: Load user name from Firestore
            binding.tvLastMessage.text = chat.lastMessage
            binding.tvTime.text = formatTime(chat.lastMessageTime)

            if (chat.unreadCount > 0) {
                binding.tvUnread.visibility = android.view.View.VISIBLE
                binding.tvUnread.text = chat.unreadCount.toString()
            } else {
                binding.tvUnread.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onChatClick(chat) }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val today = java.util.Calendar.getInstance()

        val msgCalendar = java.util.Calendar.getInstance().apply { time = date }

        return if (today.get(java.util.Calendar.DAY_OF_YEAR) == msgCalendar.get(java.util.Calendar.DAY_OF_YEAR) &&
            today.get(java.util.Calendar.YEAR) == msgCalendar.get(java.util.Calendar.YEAR)
        ) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean =
            oldItem.chatId == newItem.chatId

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean =
            oldItem == newItem
    }
}
