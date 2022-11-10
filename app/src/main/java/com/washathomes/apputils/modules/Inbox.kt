package com.washathomes.apputils.modules

data class InboxMessages(val status: Status, val results: ArrayList<Inbox>)

data class Inbox(val id: String, val status: String, val washee_id: String, val washer_id: String, val courier_id: String, val date_created: String, val buyer_profile_picture: String, val seller_profile_picture: String, val driver_profile_picture: String)
