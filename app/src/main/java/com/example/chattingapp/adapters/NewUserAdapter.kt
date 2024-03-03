package com.example.chattingapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chattingapp.databinding.ItemContainerUserBinding
import com.example.chattingapp.listeners.UserListener
import com.example.chattingapp.model.User
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.ArrayList

class NewUserAdapter() : RecyclerView.Adapter<NewUserAdapter.UserViewHolder>() {
//    (val context:Context, private var userList: ArrayList<User>)

    private lateinit var userList: ArrayList<User>
    private lateinit var userListener: UserListener

    constructor(userList: ArrayList<User>, userListener: UserListener) : this() {
        this.userList = userList
        this.userListener = userListener
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemContainerUserBinding.bind(itemView)
//        val auth = Firebase.auth
        fun setUserData (user: User){
            binding.textName.text = user.username
            binding.bio.text = user.bio
            binding.root.setOnClickListener(){userListener.onUserClicked(user)}
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("Profile").child(user.uid!!)
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

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(itemContainerUserBinding.root)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.setUserData(user)
    }
}