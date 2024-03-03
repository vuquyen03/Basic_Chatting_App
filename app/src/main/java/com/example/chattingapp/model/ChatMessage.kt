package com.example.chattingapp.model

import java.util.Date

class ChatMessage() {
    var message: String? = null
    var senderID: String? = null
    var receiverId: String? = null
    var dateTime: String? = null
    var dateObject: Date? = null

    constructor(
        message: String?,
        senderID: String?,
        receiverId: String?,
        dateTime: String?
    ) : this() {
        this.message = message
        this.senderID = senderID
        this.receiverId = receiverId
        this.dateTime = dateTime
    }
}