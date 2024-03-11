package com.example.chattingapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import com.example.chattingapp.adapters.RecentConversationAdapter
import com.example.chattingapp.databinding.ActivityMainBinding
import com.example.chattingapp.listeners.ConversionListener
import com.example.chattingapp.model.ChatMessage
import com.example.chattingapp.model.User
import com.example.chattingapp.ui.base.BaseStatus
import com.example.chattingapp.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class MainActivity : BaseStatus(), ConversionListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private var email: String? = null
    private lateinit var conversations: MutableList<ChatMessage>
    private lateinit var database: FirebaseFirestore
    private lateinit var conversationsAdapter: RecentConversationAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        email = sharedPreferences.getString(Constants.EMAIL_KEY, null)
        auth = Firebase.auth
        sharedPreferences.edit().putString(Constants.KEY_USER_ID, auth.uid).apply()
        init()

        getInformation()
        setListener()
        listenConversations()
    }

    private fun init(){
        conversations = ArrayList()
        conversationsAdapter = RecentConversationAdapter(conversations, sharedPreferences, this)
        binding.conversationRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun listenConversations(){
        database.collection(Constants.KEY_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, sharedPreferences.getString(Constants.KEY_USER_ID, null))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, sharedPreferences.getString(Constants.KEY_USER_ID, null))
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (document in value.documentChanges) {
                if(document.type == DocumentChange.Type.ADDED){
                    val chatMessage = ChatMessage()
                    val senderId = document.document.getString(Constants.KEY_SENDER_ID)
                    val receiverId = document.document.getString(Constants.KEY_RECEIVER_ID)
                    chatMessage.senderID = senderId
                    chatMessage.receiverId = receiverId
                    if (sharedPreferences.getString(Constants.KEY_USER_ID, null) == senderId) {
                        chatMessage.conversionName = document.document.getString(Constants.KEY_RECEIVER_NAME)
                        chatMessage.conversionId = document.document.getString(Constants.KEY_RECEIVER_ID)
                    } else {
                        chatMessage.conversionName = document.document.getString(Constants.KEY_SENDER_NAME)
                        chatMessage.conversionId = document.document.getString(Constants.KEY_SENDER_ID)
                    }
                    chatMessage.message = document.document.getString(Constants.KEY_LAST_MESSAGE)
                    chatMessage.dateObject = document.document.getDate(Constants.KEY_TIMESTAMP)
                    conversations.add(chatMessage)
                }else if (document.type == DocumentChange.Type.MODIFIED){
                    for (i in 0 until conversations.size){
                        val senderId = document.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId = document.document.getString(Constants.KEY_RECEIVER_ID)
                        if (conversations[i].senderID == senderId && conversations[i].receiverId == receiverId){
                            conversations[i].message = document.document.getString(Constants.KEY_LAST_MESSAGE)
                            conversations[i].dateObject = document.document.getDate(Constants.KEY_TIMESTAMP)
                            break
                        }
                    }
                }
            }
            conversations.sortWith { o1, o2 -> o2.dateObject!!.compareTo(o1.dateObject!!) }
            conversationsAdapter.notifyDataSetChanged()
            binding.conversationRecyclerView.scrollToPosition(0)
            binding.conversationRecyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun getImageFromStorage(){
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("Profile").child(sharedPreferences.getString(Constants.KEY_USER_ID, null)!!)
        val oneMB = 1024 * 1024.toLong()

        imageRef.getBytes(oneMB)
            .addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imageProfile.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun getNameFromDatabase() {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(sharedPreferences.getString(Constants.KEY_USER_ID, null)!!)
        userRef.get().addOnSuccessListener {
            val name = it.child("username").value.toString()
            sharedPreferences.edit().putString(Constants.KEY_NAME, name).apply()
            binding.textName.text = name
        }
    }

    private fun getInformation(){
        getImageFromStorage()
        getNameFromDatabase()
    }

    private fun setListener(){
        binding.imageLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.LOGIN_KEY, false)
            editor.apply()

            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        binding.fabNewChat.setOnClickListener{
            startActivity(Intent(this, UsersActivity::class.java))
        }
    }

    override fun onConversionClicked(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}