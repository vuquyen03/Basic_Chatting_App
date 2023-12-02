package com.example.chattingapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chattingapp.R
import com.example.chattingapp.authentication.SetupInformation
import com.example.chattingapp.databinding.FragmentSignUpBinding
import com.example.chattingapp.ui.base.BaseFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpFragment : BaseFragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.tvLoginPage.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        binding.btnSignUp.setOnClickListener{
            registrationUser()
        }

        return binding.root
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
                        val intent = Intent(requireActivity(), SetupInformation::class.java)
                        requireActivity().startActivity(intent)
                        requireActivity().finish()
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