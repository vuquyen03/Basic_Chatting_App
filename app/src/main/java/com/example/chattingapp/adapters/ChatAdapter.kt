package com.example.chattingapp.adapters

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.ItemContainerReceivedMessageBinding
import com.example.chattingapp.databinding.ItemContainerSentMessageBinding
import com.example.chattingapp.model.ChatMessage
import com.example.chattingapp.model.User
import com.google.firebase.storage.FirebaseStorage

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private var chatMessages: MutableList<ChatMessage>
    private var receiver: User
    private lateinit var senderID: String
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

//    constructor(chatMessages: List<ChatMessage>, senderID: String?) {
//        if (senderID != null) {
//            this.senderID = senderID
//        }
//        this.chatMessages = chatMessages
//    }

    constructor(chatMessages: MutableList<ChatMessage>, receiver: User, senderID: String?) {
        if (senderID != null) {
            this.senderID = senderID
        }
        this.chatMessages = chatMessages
        this.receiver = receiver
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT){
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(chatMessages[position], receiver)
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage, receiver: User) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("Profile").child(receiver.uid!!)
            val oneMB = 1024 * 1024.toLong()

            imageRef.getBytes(oneMB)
                .addOnSuccessListener { bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.imageProfile.setImageBitmap(bitmap)
                }
                .addOnFailureListener { exception ->
                    // Xử lý lỗi khi không thể tải ảnh
                    exception.printStackTrace()
                }
        }
    }

    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(chatMessages[position].senderID == senderID){
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
}
