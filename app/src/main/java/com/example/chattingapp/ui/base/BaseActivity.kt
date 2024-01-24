package com.example.chattingapp.ui.base

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chattingapp.R
import com.example.chattingapp.databinding.ActivityBaseBinding

open class BaseActivity : AppCompatActivity() {
    private lateinit var pb: Dialog
    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun showProgressBar(){
        pb = Dialog(this)
        pb.setContentView(R.layout.progress_bar)
        pb.setCancelable(false)
        pb.show()
    }

    fun hideProgressBar(){
        pb.hide()
    }

    fun showToast(activity: Activity, msg: String){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    // Phương thức tùy chỉnh để giải phóng tài nguyên trước khi Activity kết thúc
    open fun onDestroyCustom() {
        // Giải phóng Dialog nếu đang hiển thị
        if (::pb.isInitialized && pb.isShowing) {
            pb.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onDestroyCustom()
    }
}