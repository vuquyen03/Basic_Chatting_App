package com.example.chattingapp.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import com.example.chattingapp.adapters.NewUserAdapter
import com.example.chattingapp.databinding.ActivityUsersBinding
import com.example.chattingapp.listeners.UserListener
import com.example.chattingapp.model.User
import com.example.chattingapp.util.Constants
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase

class UsersActivity : AppCompatActivity(), UserListener {
    private lateinit var binding: ActivityUsersBinding
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setListener()
        getUsers()
    }

    private fun setListener(){
        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUsers(){
        loading(true)
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users")
        userRef.get().addOnCompleteListener(){
            if(it.isSuccessful){
                val list = ArrayList<User>()
                val auth = Firebase.auth
                for(user in it.result!!.children){
                    val customer = user.getValue(User::class.java)
                    if(customer!!.uid != auth.uid){
                        list.add(customer)
                        Log.d("TAG", "getUsers: ${customer.username}")
                    }
                }
                if(list.size > 0){
                    binding.usersRecyclerView.adapter = NewUserAdapter(list, this)
                    binding.usersRecyclerView.visibility = View.VISIBLE
                }else{
                    showErrorMessage()
                }
                Log.d("TAG", "getUsers: ${list.size}")
            }else{
                showErrorMessage()
            }
            loading(false)
        }
    }

    private fun showErrorMessage(){
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean){
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    @Override
    override fun onUserClicked(user: User){
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}