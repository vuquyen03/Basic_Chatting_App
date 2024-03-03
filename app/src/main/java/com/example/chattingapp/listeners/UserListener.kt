package com.example.chattingapp.listeners

import com.example.chattingapp.model.User

interface UserListener {
    fun onUserClicked(user: User)
}