package com.example.chattingapp.ui.auth
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chattingapp.MainActivity
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentSignInBinding
import com.example.chattingapp.ui.base.BaseFragment
import com.example.chattingapp.util.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SignInFragment : BaseFragment() {

    private val googleSignInRequestCode = 234
    private lateinit var binding: FragmentSignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)

        // Initialize Firebase Auth
        auth = Firebase.auth
        mAuth = FirebaseAuth.getInstance()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isLoggedIn = sharedPreferences.getBoolean(Constants.LOGIN_KEY, false)
        if (isLoggedIn){
            val intent = Intent(requireActivity(), MainActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_forgetPasswordFragment)
        }
        binding.btnSignIn.setOnClickListener{
            signInUser()
        }

        binding.btnSignInWithGoogle.setOnClickListener{
            signInByGoogle()
        }
        return binding.root
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
                        autoLogin(true, email)
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        requireActivity().startActivity(intent)
                        requireActivity().finish()
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

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        if (isConnected(requireContext())){

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
        showProgressBar()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()){ task ->
                if(task.isSuccessful){
                    hideProgressBar()
                    autoLogin(true, account.email!!)
                    showToast(this, "Login Successful")
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    requireActivity().startActivity(intent)
                    requireActivity().finish()
                }else{
                    hideProgressBar()
                    showToast(this, "Login Failed")
                }
            }
    }

    private fun autoLogin(value: Boolean, email: String){
        val editor = sharedPreferences.edit()
        editor.putBoolean(Constants.LOGIN_KEY, value)
        editor.putString(Constants.EMAIL_KEY, email)
        editor.apply()
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