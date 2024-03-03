package com.example.chattingapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chattingapp.activities.MainActivity
import com.example.chattingapp.model.User
import com.example.chattingapp.databinding.ActivitySetupInformationBinding
import com.example.chattingapp.ui.base.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Date

class SetupInformation : BaseActivity() {
    private lateinit var binding: ActivitySetupInformationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var encodedImage: String
    private var selectedPicture: Uri ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        supportActionBar?.hide()
        binding.imageProfile.setOnClickListener{
            chooseImage()
        }

        binding.btnDone.setOnClickListener{
            showProgressBar()
            val name = binding.etUsername.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            if (name.isEmpty()){
                hideProgressBar()
                showToast(this, "Name is required")
            } else if (bio.isEmpty()){
                hideProgressBar()
                showToast(this, "Bio is required")
            } else if (selectedPicture != null){
                val reference = storage.reference.child("Profile")
                    .child(auth.uid!!)


                reference.putFile(selectedPicture!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
//                        showToast(this, "SuccessFul")
                        reference.downloadUrl.addOnCompleteListener { uri ->
                            val imageUri = uri.toString()
                            val uid = auth.uid
                            val user = User(uid, name, bio, imageUri)
                            database.reference.child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener{
                                    hideProgressBar()
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
                            .addOnCanceledListener{
                                hideProgressBar()
                                showToast(this, "Failed")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
            } else if (selectedPicture == null) {
                hideProgressBar()
                showToast(this, "Image is required")
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
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // Khi chọn ảnh thành công
            binding.addImage.visibility = View.GONE
        }

        if (data != null) {
            if(data.data != null){
                showProgressBar()
                selectedPicture = data.data
                val time = Date().time
                val reference = storage.reference.child("Profile")
                    .child(time.toString() +"")
                reference.putFile(selectedPicture!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            reference.downloadUrl.addOnCompleteListener {uri ->
                                hideProgressBar()
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
                        else {
                            showToast(this, "Failed to Upload Image")
                        }
                    }
                binding.imageProfile.setImageURI(selectedPicture)
            }
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputSteam = java.io.ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputSteam)
        val bytes = byteArrayOutputSteam.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            // Sử dụng Uri để tải ảnh
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                binding.imageProfile.setImageBitmap(bitmap)
                binding.addImage.visibility = View.GONE
                encodedImage = encodeImage(bitmap!!)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}