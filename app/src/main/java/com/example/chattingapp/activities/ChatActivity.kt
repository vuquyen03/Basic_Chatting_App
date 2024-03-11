package com.example.chattingapp.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.example.chattingapp.adapters.ChatAdapter
import com.example.chattingapp.databinding.ActivityChatBinding
import com.example.chattingapp.model.ChatMessage
import com.example.chattingapp.model.User
import com.example.chattingapp.ui.base.BaseStatus
import com.example.chattingapp.util.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Locale

class ChatActivity : BaseStatus() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var database: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private var conversionId: String? = null
    private var isReceiverAvailable:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        loadReceiverDetails()
        init(receiverUser)
        listenMessages()
    }

    private fun init(user: User){
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            user,
            sharedPreferences.getString(Constants.KEY_USER_ID, "null")
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage(){
        val message = HashMap<String, Any?>()
        message[Constants.KEY_SENDER_ID] = sharedPreferences.getString(Constants.KEY_USER_ID, null)
        message[Constants.KEY_RECEIVER_ID] = receiverUser.uid
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Timestamp(Date())
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if(conversionId != null){
            updateConversion(binding.inputMessage.text.toString())
        } else {
            var conversion = HashMap<String, Any>()
            conversion[Constants.KEY_SENDER_ID] = sharedPreferences.getString(Constants.KEY_USER_ID, null)!!
            conversion[Constants.KEY_SENDER_NAME] = sharedPreferences.getString(Constants.KEY_NAME, "null")!!
            conversion[Constants.KEY_RECEIVER_ID] = receiverUser.uid!!
            conversion[Constants.KEY_RECEIVER_NAME] = receiverUser.username
            conversion[Constants.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversion[Constants.KEY_TIMESTAMP] = Timestamp(Date())
            addConversion(conversion)
        }
        binding.inputMessage.setText("")
    }

    private fun listenAvailability() {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userRef = databaseRef.child("users").child(receiverUser.uid!!)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild(Constants.KEY_AVAILABILITY)) {
                    val status = dataSnapshot.child(Constants.KEY_AVAILABILITY).value.toString().toInt()
                    isReceiverAvailable = status == 1

                    if (isReceiverAvailable) {
                        binding.textStatus.text = "Online"
                        binding.textStatus.visibility = View.VISIBLE
                    } else {
                        binding.textStatus.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        })
    }

    private fun listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, sharedPreferences.getString(Constants.KEY_USER_ID, "null"))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.uid)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.uid)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, sharedPreferences.getString(Constants.KEY_USER_ID, "null"))
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
            chatMessages.sortWith { obj1, obj2 ->
                obj1.dateObject!!.compareTo(obj2.dateObject!!)
            }
            if (count == 0){
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
        if(conversionId == null){
            checkForConversion()
        }
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

    private fun addConversion(conversion: HashMap<String, Any>){
        database.collection(Constants.KEY_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener {
                conversionId = it.id
            }
    }

    private fun updateConversion(message: String){
        val documentReference = database.collection(Constants.KEY_CONVERSATIONS).document(conversionId!!)
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Timestamp(Date()))
    }

    private fun checkForConversion(){
        if(chatMessages.size != 0) {
            checkForConversionRemotely(sharedPreferences.getString(Constants.KEY_USER_ID, null)!!, receiverUser.uid!!)
            checkForConversionRemotely(receiverUser.uid!!, sharedPreferences.getString(Constants.KEY_USER_ID, null)!!)
        }
    }

    private fun checkForConversionRemotely(senderId: String, receiverId: String){
        database.collection(Constants.KEY_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener = OnCompleteListener<QuerySnapshot>{ task ->
        if(task.isSuccessful && task.result != null && task.result!!.documents.size > 0){
            val document = task.result!!.documents[0]
            conversionId = document.id
        }
    }

    override fun onResume() {
        super.onResume()
        listenAvailability()
    }
}