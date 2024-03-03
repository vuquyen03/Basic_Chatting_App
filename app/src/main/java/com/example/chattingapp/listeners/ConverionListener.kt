package com.example.chattingapp.listeners

import com.example.chattingapp.model.User

interface ConversionListener {
    fun onConversionClicked(user: User)
}