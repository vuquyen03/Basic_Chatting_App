package com.example.chattingapp.model

data class User (var uid: String?,
                 var username: String,
                 var bio: String?,
                 val profileImage: String): java.io.Serializable {
    constructor(): this("","", "", "")
}