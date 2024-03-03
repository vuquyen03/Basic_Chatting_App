package com.example.chattingapp.activities

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.chattingapp.adapters.ChatAdapter
import com.example.chattingapp.databinding.ActivityChatBinding
import com.example.chattingapp.model.ChatMessage
import com.example.chattingapp.model.User
import com.example.chattingapp.util.Constants
import com.example.chattingapp.util.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.HashMap
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        auth = Firebase.auth

        loadReceiverDetails()
        init(receiverUser)
        listenMessages()
    }

    private fun init(user: User){
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            user,
            auth.uid
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage(){
        val message = HashMap<String, Any?>()
        message[Constants.KEY_SENDER_ID] = auth.uid
        message[Constants.KEY_RECEIVER_ID] = receiverUser.uid
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Timestamp(Date())
        binding.inputMessage.setText("")
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
    }

    private fun listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, auth.uid)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.uid)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.uid)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, auth.uid)
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null){
            return@EventListener
        }
        if (value != null){
            val count = chatMessages.size
            for (documentChange in value.documentChanges){
                if (documentChange.type == DocumentChange.Type.ADDED){
                    val chatMessage = ChatMessage()
                    chatMessage.senderID = documentChange.document.getString(Constants.KEY_SENDER_ID)
                    chatMessage.receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                    chatMessage.message = documentChange.document.getString(Constants.KEY_MESSAGE)
//                    chatMessage.dateTime = documentChange.document.getDate(Constants.KEY_TIMESTAMP).toString()
                    val timestamp = documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                    val dateTime = if (timestamp != null) {
                        getReadableDateTime(timestamp)
                    } else {
                        null
                    }
                    chatMessage.dateTime = dateTime
                    chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)

                    chatMessages.add(chatMessage)
                }
            }
            chatMessages.sortWith(Comparator { obj1, obj2 ->
                obj1.dateObject!!.compareTo(obj2.dateObject!!)
            })
            if (count == 0){
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun loadReceiverDetails(){
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.textName.text = receiverUser.username
    }

    private fun setListeners(){
        binding.imageBack.setOnClickListener{
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener{
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date?): String? {
        return SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(date)
    }
}