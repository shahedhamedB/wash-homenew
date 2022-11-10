package com.washathomes.apputils.modules

data class Notification(val id: String, val data_id: String, val notify_type: String, val is_read: String, val title: String, val details: String, val date: String, val time: String, val status: String)

data class NotificationObj(val count: String, val notifications: ArrayList<Notification>)

data class Notifications(val status: Status, val results: NotificationObj)

data class NotificationId(val id: String)
