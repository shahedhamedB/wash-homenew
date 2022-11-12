package com.washathomes.apputils.modules

data class ChatRoom(val buyerId: String, val driverId: String, val orderId: String, val sellerId: String, val messages: ArrayList<Message>)

data class Message(val createTime: String, val message: String, val senderId: String)
