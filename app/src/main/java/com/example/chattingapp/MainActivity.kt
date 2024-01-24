package com.example.chattingapp

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import com.example.chattingapp.adapter.userAdapter
import com.example.chattingapp.databinding.ActivityMainBinding
import com.example.chattingapp.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var usersAdapter: userAdapter
    private var email: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        loadUserDetails()
        email = sharedPreferences.getString(Constants.EMAIL_KEY, null)

        auth = Firebase.auth
        binding.imageLogout.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean(Constants.LOGIN_KEY, false)
            editor.apply()

            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun loadUserDetails(){
        binding.textName.text = sharedPreferences.getString(Constants.KEY_NAME, null)
        val byte = Base64.decode(sharedPreferences.getString(Constants.KEY_IMAGE, null), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(byte, 0, byte.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }
}