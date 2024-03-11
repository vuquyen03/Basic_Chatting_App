package com.example.chattingapp.adapters

import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.ItemContainerRecentConversationBinding
import com.example.chattingapp.listeners.ConversionListener
import com.example.chattingapp.model.ChatMessage
import com.example.chattingapp.model.User
import com.example.chattingapp.util.Constants
import com.google.firebase.storage.FirebaseStorage

class RecentConversationAdapter(
    private var chatMessages: MutableList<ChatMessage>,
    private val sharePreferences: SharedPreferences,
    private val conversionListener: ConversionListener
) : RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder>() {

    inner class ConversionViewHolder(private val binding: ItemContainerRecentConversationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            val storageRef = FirebaseStorage.getInstance().reference
            var uid: String ?= null
            uid = if (sharePreferences.getString(Constants.KEY_USER_ID, null) == chatMessage.senderID!!){
                chatMessage.receiverId
            } else {
                chatMessage.senderID
            }
            val imageRef = storageRef.child("Profile").child(uid!!)
            val oneMB = 1024 * 1024.toLong()

            imageRef.getBytes(oneMB)
                .addOnSuccessListener { bytes ->
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.imageProfile.setImageBitmap(bitmap)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }

            binding.root.setOnClickListener{
                val user: User = User()
                user.uid = chatMessage.conversionId
                user.username = chatMessage.conversionName.toString()
                conversionListener.onConversionClicked(user)
            }
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemContainerRecentConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position] )
    }

}