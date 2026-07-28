package tj.safaraligroup.sim.ui.components

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tj.safaraligroup.sim.data.model.Message
import tj.safaraligroup.sim.databinding.ItemMessageBinding
import tj.safaraligroup.sim.databinding.ItemMessageCallBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val TYPE_TEXT = 1
        private const val TYPE_CALL = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when (message.messageType) {
            Message.TYPE_TEXT, Message.TYPE_IMAGE -> TYPE_TEXT
            else -> TYPE_CALL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT -> {
                val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextMessageViewHolder(binding)
            }
            TYPE_CALL -> {
                val binding = ItemMessageCallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CallMessageViewHolder(binding)
            }
            else -> {
                val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextMessageViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        val isMine = message.senderId == currentUserId

        when (holder) {
            is TextMessageViewHolder -> holder.bind(message, isMine)
            is CallMessageViewHolder -> holder.bind(message, isMine)
        }
    }

    inner class TextMessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, isMine: Boolean) {
            binding.tvMessage.text = message.text
            binding.tvTime.text = formatTime(message.timestamp)

            // Check/status indicator
            if (isMine) {
                binding.ivStatus.visibility = android.view.View.VISIBLE
                binding.ivStatus.setImageResource(
                    if (message.isSeen) tj.safaraligroup.sim.R.drawable.ic_double_check_blue
                    else if (message.isDelivered) tj.safaraligroup.sim.R.drawable.ic_double_check_gray
                    else tj.safaraligroup.sim.R.drawable.ic_check_gray
                )
            } else {
                binding.ivStatus.visibility = android.view.View.GONE
            }

            // Alignment
            val params = binding.root.layoutParams as LinearLayout.LayoutParams
            if (isMine) {
                params.gravity = Gravity.END
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(tj.safaraligroup.sim.R.color.message_outgoing)
                )
            } else {
                params.gravity = Gravity.START
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(tj.safaraligroup.sim.R.color.message_incoming)
                )
            }
            binding.root.layoutParams = params
        }
    }

    inner class CallMessageViewHolder(private val binding: ItemMessageCallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, isMine: Boolean) {
            val callIcon = when (message.messageType) {
                Message.TYPE_CALL_START -> "📞"
                Message.TYPE_CALL_END -> "✅"
                Message.TYPE_CALL_MISSED -> "❌"
                else -> "📞"
            }

            binding.tvCallInfo.text = "$callIcon ${message.text}"
            binding.tvCallTime.text = formatTime(message.timestamp)

            val params = binding.root.layoutParams as LinearLayout.LayoutParams
            params.gravity = Gravity.CENTER
            binding.root.layoutParams = params
        }
    }

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
            oldItem.messageId == newItem.messageId

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
            oldItem == newItem
    }
}
