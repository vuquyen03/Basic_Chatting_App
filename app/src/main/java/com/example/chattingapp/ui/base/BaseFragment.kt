package com.example.chattingapp.ui.base

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chattingapp.R
import com.example.chattingapp.databinding.FragmentBaseBinding

open class BaseFragment : Fragment() {
    private lateinit var pb: Dialog
    private lateinit var binding: FragmentBaseBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    fun showProgressBar(){
        pb = Dialog(requireContext())
        pb.setContentView(R.layout.progress_bar)
        pb.setCancelable(false)
        pb.show()
    }

    fun hideProgressBar(){
        pb.hide()
    }

    fun showToast(fragment: Fragment, msg: String){
        Toast.makeText(fragment.requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

}