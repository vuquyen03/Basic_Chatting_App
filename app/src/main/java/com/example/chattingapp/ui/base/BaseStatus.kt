package com.example.chattingapp.ui.base

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.example.chattingapp.util.Constants
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

open class BaseStatus : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val database = FirebaseDatabase.getInstance().reference
        databaseReference = database.child("users").child(sharedPreferences.getString(Constants.KEY_USER_ID, null)!!)
    }

    override fun onPause() {
        super.onPause()
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_AVAILABILITY] = 0

        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                // Cập nhật thành công
            }
    }

    override fun onResume() {
        super.onResume()
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_AVAILABILITY] = 1

        databaseReference.updateChildren(updates)
            .addOnSuccessListener {
                // Cập nhật thành công
            }
    }
}