package com.washathomes.apputils.modules.chatmodel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created on 2019-10-16.
 */

@Parcelize
data class ChatRoom(val orderId : String = "0",
                    var roomKey : String = "",
                    var buyerId : String = "0",
                    var driverId : String = "0",
                    var sellerId : String = "0",
                    var messages : ArrayList<ChatMessage>,
                    var order : Order? = null) : Parcelable {

    constructor() : this(orderId="0", roomKey="", buyerId="0", sellerId="0", driverId="0", messages=ArrayList())

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "orderId" to orderId,
            "buyerId" to buyerId,
            "driverId" to driverId,
            "sellerId" to sellerId,
            "messages" to getMessagesMap()
        )
    }

    private fun getMessagesMap() : ArrayList<HashMap<String, Any?>> {
        val mMap = ArrayList<HashMap<String, Any?>>()
        for(message in messages) {
            mMap.add(message.toMap())
        }
        return mMap
    }
}

@Parcelize
data class ChatMessage(val message : String = "",
                       val senderId : Int = 0,
                       val createTime : Long = 0) : Parcelable {

    fun toMap(): HashMap<String, Any?> {
        return hashMapOf(
            "message" to message,
            "senderId" to senderId,
            "createTime" to createTime
        )
    }

}