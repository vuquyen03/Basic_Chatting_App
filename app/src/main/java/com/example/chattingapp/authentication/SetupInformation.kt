package com.example.chattingapp.authentication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.example.chattingapp.MainActivity
import com.example.chattingapp.model.User
import com.example.chattingapp.databinding.ActivitySetupInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupInformation : BaseActivity() {
    private lateinit var binding: ActivitySetupInformationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedPicture: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        supportActionBar?.hide()
        binding.imageProfile.setOnClickListener{
            chooseImage()
        }

        binding.btnDone.setOnClickListener{
            val name = binding.etUsername.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            if (selectedPicture != null){
                val reference = storage.reference.child("Profile")
                    .child(auth.uid!!)

                reference.putFile(selectedPicture).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val imageUri = uri.toString()
                            val uid = auth.uid
                            val user = User(uid, name, bio, imageUri)
                            database.reference.child("Users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener{
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                    else{
                        val uid = auth.uid
                        val user = User(uid, name, bio, "No Image")
                        database.reference.child("users")
                            .child(uid!!)
                            .setValue(user)
                            .addOnCompleteListener{
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            }
            if (name.isEmpty()){
                showToast(this, "Name is required")
            } else if (bio.isEmpty()){
                showToast(this, "Bio is required")
            }
//            } else {
//                showProgressBar()
//                val ref = storage.getReference("/images/$uid")
//                ref.putFile(selectedPicture)
//                    .addOnSuccessListener {
//                        ref.downloadUrl.addOnSuccessListener {
//                            val profilePicturePath = it.toString()
//                            val user = User(name, bio, profilePicturePath)
//                            database.getReference("/users/$uid")
//                                .setValue(user)
//                                .addOnSuccessListener {
//                                    hideProgressBar()
//                                    val intent = Intent(this, MainActivity::class.java)
//                                    startActivity(intent)
//                                    finish()
//                                }
//                        }
//                    }
//                    .addOnFailureListener{
//                        showToast(this, "Failed to upload image")
//                        hideProgressBar()
//                    }
//            }
        }
    }

    private fun chooseImage(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // Khi chọn ảnh thành công
            binding.addImage.visibility = View.GONE
        }

        if (data != null) {
            if(data.data != null){
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference.child("Profile")
                    .child(time.toString() +"")
                reference.putFile(uri!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            reference.downloadUrl.addOnSuccessListener {uri ->
                                val filePath = uri.toString()
                                val obj = HashMap<String,Any>()
                                obj["image"] = filePath
                                database.reference.child("users")
                                    .child(auth.uid!!)
                                    .updateChildren(obj)
                                    .addOnSuccessListener {
                                        showToast(this, "Image Uploaded")
                                    }
                            }
                        }
                    }
                binding.imageProfile.setImageURI(uri)
                selectedPicture = uri
            }
        }
    }
}