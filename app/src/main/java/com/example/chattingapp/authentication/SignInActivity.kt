package com.example.chattingapp.authentication

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import com.example.chattingapp.MainActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignInActivity : BaseActivity() {
    private val googleSignInRequestCode = 234
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mAuth: FirebaseAuth

    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.

//        val user = mAuth.currentUser
//        if(user != null){
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        mAuth = FirebaseAuth.getInstance()

        binding.tvRegister.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvForgotPassword.setOnClickListener{
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignIn.setOnClickListener{
            signInUser()
        }

        binding.btnSignInWithGoogle.setOnClickListener{
            signInByGoogle()
        }
    }

    private fun signInUser(){
        val email = binding.etSignInEmail.text.toString().trim()
        val password = binding.etSignInPassword.text.toString().trim()
        if(validateForm(email, password)){
            showProgressBar()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        hideProgressBar()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else{
                        showToast(this, "Login Failed")
                        hideProgressBar()
                    }
                }
        }
    }

    private fun validateForm(email: String, password:String): Boolean{
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()){
            showToast(this, "Please enter a valid email")
            return false
        }
        if(password.isEmpty()){
            showToast(this, "Password is required")
            return false
        }
        return true
    }

    private fun signInByGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        if (isConnected(this)){

            // The signInIntent is used to handle the sign in process
            val signInIntent =  googleSignInClient.signInIntent

            startActivityForResult(signInIntent,googleSignInRequestCode)
        }else{
            showToast(this, "No Internet Connection!")
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    showToast(this, "Login Successful")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    showToast(this, "Login Failed")
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode){
            googleSignInRequestCode -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)

                }catch (e: ApiException){
                    when (e.statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {
                            showToast(this, "Login cancelled")
                        }
                        GoogleSignInStatusCodes.SIGN_IN_FAILED -> {
                            // Xử lý lỗi xác thực
                            showToast(this, "Confirm failed")

                        }
                        else -> {
                            // Xử lý lỗi khác
                            showToast(this, "Other error")
                        }
                    }
                }
            }
        }
    }

}