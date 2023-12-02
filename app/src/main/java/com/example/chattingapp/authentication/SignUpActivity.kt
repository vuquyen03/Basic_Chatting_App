package com.example.chattingapp.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import com.example.chattingapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : BaseActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.tvLoginPage.setOnClickListener{
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener{
            registrationUser()
        }
    }

    private fun registrationUser (){
        val email = binding.etSignUpEmail.text.toString()
        val password = binding.etSignUpPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (validateForm(email, password, confirmPassword)){
            showProgressBar()
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful) {
                        hideProgressBar()
                        val intent = Intent(this, SetupInformation::class.java)
                        startActivity(intent)
                        finish()
                    } else{
                        showToast(this, "Registration Failed")
                        hideProgressBar()
                    }
                }
        }
    }

    private fun validateForm (email: String, password: String?, confirmPassword: String?) : Boolean{

        if (email.isEmpty()){
            showToast(this, "Email is required")
            return false
        }
        // Check format of email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showToast(this, "Please enter a valid email")
            return false
        }

        if (password != null) {
            if (password.trim().isNullOrEmpty()){
                showToast(this, "Password is required")
                return false
            }
        }

        if (confirmPassword.isNullOrEmpty()){
            showToast(this, "Confirm Password is required")
            return false
        }

        if (confirmPassword != password){
            binding.etConfirmPassword.error = "Password and Confirm Password must be same"
            return false
        }

        return true
    }
}