package com.example.chattingapp.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.example.chattingapp.databinding.ActivityMainBinding
import com.example.chattingapp.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private var email: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        email = sharedPreferences.getString(Constants.EMAIL_KEY, null)

        auth = Firebase.auth

        getInformation()

        setListener()
    }

    private fun getImageFromStorage(){
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("Profile").child(auth.uid!!)
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

    private fun getNameFromDatabase(){
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(auth.uid!!)
        userRef.get().addOnSuccessListener {
            val name = it.child("username").value.toString()
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
}